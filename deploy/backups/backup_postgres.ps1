param(
    [string]$modo = "normal",
    [string]$envPath = "C:\ruta\tu\.env"
)

# 🧠 Cargar .env
function Load-Env($path) {
    Get-Content $path | ForEach-Object {
        if ($_ -match "^\s*#") { return }
        if ($_ -match "^\s*$") { return }

        $name, $value = $_ -split "=", 2
        [System.Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim())
    }
}

Load-Env $envPath

# 📦 Variables desde .env
$carpetaLocal = $env:BACKUP_LOCAL_PATH
$carpetaNube  = $env:BACKUP_CLOUD_PATH
$container    = $env:DB_CONTAINER_NAME
$db           = $env:DB_NAME
$user         = $env:DB_USER
$retencion    = [int]$env:RETENTION_DAYS

# 🕓 Fecha
$fecha = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"

# 📁 Nombre archivo
if ($modo -eq "apagado") {
    $archivoFinal = "respaldo_${fecha}_apagado.sql"
} else {
    $archivoFinal = "respaldo_${fecha}.sql"
}

$rutaFinalLocal = Join-Path $carpetaLocal $archivoFinal
$rutaTempLocal  = "$rutaFinalLocal.partial"
$rutaFinalNube  = Join-Path $carpetaNube $archivoFinal

# 📂 Crear carpetas
foreach ($ruta in @($carpetaLocal, $carpetaNube)) {
    if (!(Test-Path $ruta)) {
        New-Item -ItemType Directory -Path $ruta -Force | Out-Null
    }
}

# 🧹 Limpiar temporal
if (Test-Path $rutaTempLocal) {
    Remove-Item $rutaTempLocal -Force
}

# 🚀 BACKUP DESDE DOCKER
cmd /c "docker exec $container pg_dump -U $user $db > `"$rutaTempLocal`""

# ✅ Validación
if ($LASTEXITCODE -eq 0 -and (Test-Path $rutaTempLocal)) {

    Move-Item $rutaTempLocal $rutaFinalLocal -Force

    # ☁️ Copia a nube
    Copy-Item $rutaFinalLocal $rutaFinalNube -Force

    # 🧹 Limpieza por días
    $limite = (Get-Date).AddDays(-$retencion)

    Get-ChildItem "$carpetaLocal\respaldo_*.sql" -File |
        Where-Object { $_.LastWriteTime -lt $limite } |
        Remove-Item -Force

    Get-ChildItem "$carpetaNube\respaldo_*.sql" -File |
        Where-Object { $_.LastWriteTime -lt $limite } |
        Remove-Item -Force

    Write-Host "✅ Backup exitoso: $archivoFinal"
    exit 0
}
else {
    Write-Host "❌ Error en backup"
    exit 1
}
#!/bin/bash

# =========================
# ⚙️ CONFIGURACIÓN INICIAL
# =========================

MODO=${1:-normal}
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_PATH="$SCRIPT_DIR/.env"

# =========================
# 📥 CARGAR .env
# =========================

if [ ! -f "$ENV_PATH" ]; then
  echo "❌ Archivo .env no encontrado en $ENV_PATH"
  exit 1
fi

set -a
source "$ENV_PATH"
set +a

# =========================
# 🔍 VALIDACIONES
# =========================

if [ -z "$BACKUP_LOCAL_PATH" ] || [ -z "$DB_CONTAINER_NAME" ] || [ -z "$DB_NAME" ] || [ -z "$DB_USER" ]; then
  echo "❌ Variables faltantes en .env"
  exit 1
fi

RETENTION_DAYS=${RETENTION_DAYS:-20}

# =========================
# 🕓 FECHA Y NOMBRE
# =========================

FECHA=$(date +"%Y-%m-%d_%H-%M-%S")

if [ "$MODO" = "apagado" ]; then
  ARCHIVO="respaldo_${FECHA}_apagado.sql"
else
  ARCHIVO="respaldo_${FECHA}.sql"
fi

RUTA_LOCAL="$BACKUP_LOCAL_PATH/$ARCHIVO"
RUTA_TEMP="$RUTA_LOCAL.partial"
RUTA_NUBE="$BACKUP_CLOUD_PATH/$ARCHIVO"

# =========================
# 📂 CREAR DIRECTORIOS
# =========================

mkdir -p "$BACKUP_LOCAL_PATH"

if [ -n "$BACKUP_CLOUD_PATH" ]; then
  mkdir -p "$BACKUP_CLOUD_PATH"
fi

# =========================
# 🧹 LIMPIAR TEMPORAL
# =========================

rm -f "$RUTA_TEMP"

# =========================
# 🚀 BACKUP DESDE DOCKER
# =========================

/usr/bin/docker exec "$DB_CONTAINER_NAME" \
  pg_dump -U "$DB_USER" "$DB_NAME" > "$RUTA_TEMP"

# =========================
# ✅ VALIDACIÓN
# =========================

if [ $? -eq 0 ] && [ -f "$RUTA_TEMP" ]; then

  mv "$RUTA_TEMP" "$RUTA_LOCAL"

  # ☁️ COPIA A NUBE (si existe)
  if [ -n "$BACKUP_CLOUD_PATH" ]; then
    cp "$RUTA_LOCAL" "$RUTA_NUBE"
  fi

  # =========================
  # 🧹 LIMPIEZA AUTOMÁTICA
  # =========================

  find "$BACKUP_LOCAL_PATH" -type f -name "respaldo_*.sql" -mtime +$RETENTION_DAYS -delete

  if [ -n "$BACKUP_CLOUD_PATH" ]; then
    find "$BACKUP_CLOUD_PATH" -type f -name "respaldo_*.sql" -mtime +$RETENTION_DAYS -delete
  fi

  echo "✅ Backup exitoso: $ARCHIVO"
  exit 0

else
  echo "❌ Error en backup"
  rm -f "$RUTA_TEMP"
  exit 1
fi
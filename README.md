# Aplicación de facturación

Sistema web de punto de venta y facturación para pequeños comercios. Incluye gestión de pedidos, productos, compras, caja, clientes, usuarios y una integración configurable con facturación electrónica.

## Tecnologías

- Backend: Java 21, Spring Boot, Spring Security, JPA, PostgreSQL y Flyway.
- Frontend: Angular 21, TypeScript y Vitest.
- Despliegue: Docker Compose y Nginx.

## Estructura

- `Backend/facturacion negocio/`: API REST, lógica de negocio, migraciones y pruebas Java.
- `Cafeteria frontend/cafeteria-frontend/`: aplicación Angular organizada por funcionalidades.
- `deploy/`: Dockerfiles, Nginx y Compose para backend, frontend y PostgreSQL.

## Desarrollo local

Requisitos: JDK 21, Node.js compatible con Angular 21, npm y PostgreSQL.

En el backend configura variables como `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET` y `APP_CRYPTO_SECRET`; consulta `application.properties` para la lista completa. Inicia y prueba el backend desde su directorio:

```bash
./mvnw spring-boot:run
./mvnw test
```

Inicia el frontend desde `Cafeteria frontend/cafeteria-frontend/`:

```bash
npm ci
npm start
npm test
npm run build
```

## Despliegue con Docker

Copia `deploy/.env.example` a `deploy/.env` y sustituye todos los valores `CHANGE_ME` por valores locales aleatorios y credenciales propias. Configura las rutas de logos y backups. El certificado DIAN que ya tengas debe residir fuera de Git; configura su ruta en `DIAN_CERTIFICATE_HOST_PATH` y define alias y contraseña. El archivo real `.env` y los certificados no se deben compartir.

Desde la raíz:

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up --build
```

La configuración de backup tiene una plantilla separada en `deploy/backups/.env.example`. No uses secretos de producción en desarrollo ni publiques datos reales de clientes.

## Pruebas

El backend usa JUnit 5; algunas pruebas de integración requieren Docker/Testcontainers. El frontend ejecuta sus pruebas con Vitest mediante `npm test`. Ejecuta las pruebas del módulo afectado antes de abrir un pull request y describe los cambios de API, base de datos o interfaz.

## Seguridad

No incluyas en Git archivos `.env`, certificados, claves privadas, credenciales, respaldos ni datos de clientes. Los secretos presentes en commits históricos requieren rotación y una decisión aparte sobre limpieza del historial antes de hacer público el repositorio.

# Variables de entorno obligatorias

Con el perfil `prod`, la aplicación se configura solo por variables
de entorno. Ningún secreto está escrito en los archivos de
propiedades (ADR-009). Verificado el 2026-10-11 arrancando el `.jar`
con las variables puestas en la terminal y sin ningún `.env`.

| Variable | Qué es | Verificación |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil activo (`prod`) | Arranca con `prod` |
| `SPRING_DATASOURCE_URL` | URL de PostgreSQL | Arranca conectando a la base |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base | `${...}` sin respaldo en `application-prod.properties` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base | `${...}` sin respaldo en `application-prod.properties` |
| `JWT_SECRET` | Clave de firma JWT, mínimo 64 caracteres | **Probado**: sin ella la app no arranca; con 62 caracteres tampoco |
| `CORS_ALLOWED_ORIGINS` | Orígenes web permitidos, separados por coma | Definida en la prueba, requerida por ADR-009 |
| `ADMIN_INITIAL_PASSWORD` | Contraseña del admin inicial | Definida en la prueba, requerida por ADR-009 |

## Comportamiento verificado

- **Sin `JWT_SECRET`:** falla con `Could not resolve placeholder
  'JWT_SECRET'`.
- **Con `JWT_SECRET` de menos de 64 caracteres:** falla con
  `JWT secret must be at least 64 characters long for HS512`.
- **Con todas las variables:** arranca y escucha en el puerto `8080`
  (el perfil `prod` no define `server.port`).

## Valores literales aceptados como deuda consciente

- `application-dev.properties`: usuario y contraseña de PostgreSQL
  (`postgres`) y un `jwtSecret` propio de desarrollo, distinto del
  `JWT_SECRET` real. Solo aplican al perfil `dev` contra `localhost`.
- `application-test.properties`: base H2 en memoria y un `jwtSecret`
  fijo de prueba. Solo existen mientras corre el test.
# Required environment variables

With the `prod` profile, the application is configured only through
environment variables. No secret is written in the properties files
(ADR-009). Verified on 2026-10-11 by starting the `.jar` with the
variables set in the terminal and without any `.env`.

| Variable | What it is | Verification |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active profile (`prod`) | Starts with `prod` |
| `SPRING_DATASOURCE_URL` | PostgreSQL URL | Starts and connects to the database |
| `SPRING_DATASOURCE_USERNAME` | Database user | `${...}` with no fallback in `application-prod.properties` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `${...}` with no fallback in `application-prod.properties` |
| `JWT_SECRET` | JWT signing key, minimum 64 characters | **Tested**: without it the app does not start; with 62 characters it does not start either |
| `CORS_ALLOWED_ORIGINS` | Allowed web origins, comma-separated | Defined in the test, required by ADR-009 |
| `ADMIN_INITIAL_PASSWORD` | Initial admin password | Defined in the test, required by ADR-009 |

## Verified behavior

- **Without `JWT_SECRET`:** fails with `Could not resolve placeholder
  'JWT_SECRET'`.
- **With a `JWT_SECRET` shorter than 64 characters:** fails with
  `JWT secret must be at least 64 characters long for HS512`.
- **With all variables:** starts and listens on port `8080`
  (the `prod` profile does not define `server.port`).

## Literal values accepted as conscious debt

- `application-dev.properties`: PostgreSQL user and password
  (`postgres`) and a development-only `jwtSecret`, different from the
  real `JWT_SECRET`. They only apply to the `dev` profile against
  `localhost`.
- `application-test.properties`: in-memory H2 database and a fixed
  test `jwtSecret`. They only exist while the test runs.
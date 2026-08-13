# Testing Notes

This project has three layers of automated tests. This document
explains what each one covers and when to run each one. For a deep
dive into the critical integration suite specifically, see
[integration-testing.md](integration-testing.md).

## The Three Layers

| Layer | What it covers | Database | Speed |
|---|---|---|---|
| **Unit tests** | Business logic in isolation (services, DTO converters, validators, exception handlers) — no Spring context, no database | none (mocked) | Fastest |
| **Repository tests** | Spring Data JPA queries against real entity mappings | H2 (in-memory) | Fast |
| **Integration tests** | Full application context: auth flows, authorization, sync, Flyway migrations, OpenAPI exposure | PostgreSQL via Testcontainers | Slower |

Repository tests use H2 for speed, but H2 differs from PostgreSQL in
SQL dialect, constraint enforcement, and transaction behavior — that
gap is exactly why the critical integration suite exists and runs
against real PostgreSQL instead. See
[integration-testing.md](integration-testing.md) for details on what
the critical suite protects.

## What's Covered Where

- **Unit**: `GlobalExceptionHandlerTest` (every exception → correct
  status code and `ErrorResponse` shape), `UserServiceTest`,
  `ReferenceServiceTest`, `RoleServiceTest`, `DTOConverterTest`,
  `FieldValidationsTest`, `JwtUtilsTest`, and controller-level tests
  like `ReferenceControllerTest` (includes import/export validation:
  unsupported format, unsupported extension, path traversal
  sanitization on export).
- **Repository**: `UserRepositoryTest`, `ReferenceRepositoryTest`.
- **Integration**: see [integration-testing.md](integration-testing.md)
  — `AuthIntegrationTest`, `ReferenceFlowIntegrationTest`,
  `ReferenceAuthorizationIntegrationTest`,
  `ReferenceSynchronizationIntegrationTest`, `OpenApiIntegrationTest`,
  plus Flyway migration validation on startup.

## When to Run What

- **While actively coding / fast local iteration:**
```bash
  mvn test
```
Runs unit + repository tests only (Surefire). Fast, no Docker
required.

- **Before opening a PR, or when the change touches auth, sync,
  migrations, or API contracts:**
```bash
  mvn verify
```
Runs everything: unit + repository tests, then the full critical
integration suite (Failsafe) against real PostgreSQL via
Testcontainers. This is the gate that must pass before merging.
Requires Docker running locally.

- **To run a single integration test while debugging:**
```bash
  mvn -Dtest=ReferenceSynchronizationIntegrationTest test
```

## Requirements

- Docker installed and running (required for `mvn verify` /
  Testcontainers; not required for `mvn test`).
- Internet access on first run, to pull the PostgreSQL Testcontainer
  image.

## Expected Result

A clean run ends with:

```text
BUILD SUCCESS
```

Any `BUILD FAILURE` on `mvn verify` before a PR should be treated as
blocking — this is the gate referenced in the Fase 1 success criteria
("the pipeline stays green").
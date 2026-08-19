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


## Fase 2 Validation Baseline

This is the single, frozen definition of "green" for every PR during
Fase 2. Same checklist locally and in CI — no ambiguity about what
counts as passing.

### Automated (CI equivalent)

```bash
mvn verify
```

Must show `BUILD SUCCESS` with the full unit + critical integration
suite passing. This is non-negotiable for every PR, regardless of
what it touches.

### Manual smoke test (for PRs touching security, validation,
observability, auth, import/export, or logging)

Run after `mvn verify` passes, before merging:

```text
1. docker-compose down -v && docker-compose up --build
2. Wait for: "Started ServerApplication in X seconds"
3. POST /api/auth/signin with invalid credentials   → 401/403 consistent?
4. GET  /api/references without JWT                 → 401?
5. GET/DELETE /api/references/{id} owned by another user → 403 or 404 per documented policy?
6. POST /api/auth/refreshtoken invalid/expired       → consistent response?
7. If the PR touches rate limiting: repeat signin    → 429?
8. If the PR touches import/export: POST with malicious filename → 400/422?
9. If the PR touches observability: check logs       → correlation ID present, valid JSON?
10. GET /v3/api-docs                                 → 200 OK?
```

If any step fails, the PR does not merge. If the PR only touches
documentation or runbooks, this is stated explicitly in the PR
description and only the relevant subset of checks is run.

### 403 vs 404 policy

A single policy must be picked and kept stable across equivalent
endpoints — the problem isn't choosing one, it's mixing both without
criteria. (Policy to be defined and documented in weeks 1-3.)
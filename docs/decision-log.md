# Decision Log

This document records the main architectural decisions made during the development of CodexRM Server.

---

## ADR-001: Use PostgreSQL as Primary Database

### Status
Accepted

### Context

The project requires a relational database capable of handling structured bibliographic reference data with strong SQL support, transactional consistency, and compatibility with production environments.

The application also requires compatibility with integration testing tools such as Testcontainers.

### Decision

Use PostgreSQL as the primary relational database for development, testing, and production environments.

### Consequences

#### Positive

- Strong SQL compliance and transactional support
- Excellent integration with Spring Boot and JPA
- Reliable production-grade database
- Compatible with Testcontainers for realistic integration testing
- Better alignment between development and production environments

#### Negative

- Requires Docker or local PostgreSQL installation
- Slightly more setup complexity than in-memory databases such as H2
- Integration tests are slower compared to pure in-memory testing

---

## ADR-002: Use JWT Authentication

### Status
Accepted

### Context

The application exposes a REST API that will be consumed by multiple clients, including web, desktop, and potentially mobile applications.

The authentication mechanism should be stateless, scalable, and compatible with HTTP-based APIs.

### Decision

Use JWT (JSON Web Token) authentication with Spring Security.

Access tokens are generated after successful authentication and sent in the Authorization header using the Bearer scheme.

### Consequences

#### Positive

- Stateless authentication mechanism
- Scalable for distributed systems and multiple clients
- Reduces server-side session management complexity
- Well supported by Spring Security
- Suitable for REST APIs

#### Negative

- Token invalidation is more complex compared to session-based authentication
- Requires careful secret key management
- Expired token handling adds additional logic
- JWT payload size increases request headers


---

## ADR-003: Adopt Layered Architecture

### Status
Accepted

### Context

As the project grew in complexity, it became necessary to separate responsibilities between API exposure, business logic, persistence, and infrastructure concerns.

A clear package structure was required to improve maintainability, readability, and testability.

### Decision

Adopt a layered architecture organized into the following main layers:

- api
- domain
- infrastructure
- component

Each layer is responsible for a specific part of the application and communicates through clearly defined boundaries.

### Consequences

#### Positive

- Better separation of concerns
- Improved code organization and readability
- Easier unit and integration testing
- Simplifies future maintenance and feature additions
- Reduces coupling between framework and business logic

#### Negative

- More classes and package structure complexity
- Additional mapping between layers
- Slightly more boilerplate code compared to simpler architectures

---

## ADR-004: Use Flyway for Database Migrations

### Status
Accepted

### Context

The project requires database schema versioning to ensure consistency across development, testing, and production environments.

Manual schema updates become difficult to maintain as the application evolves.

### Decision

Use Flyway for database migration management.

Database schema changes are versioned using SQL migration scripts executed automatically during application startup.

### Consequences

#### Positive

- Database schema is version-controlled
- Consistent database structure across environments
- Easier collaboration between developers
- Simplifies deployment and environment setup
- Works well with PostgreSQL and Testcontainers

#### Negative

- Requires maintaining migration scripts
- Incorrect migrations may affect application startup
- Schema rollback management requires additional planning

---

## ADR-005: Use Testcontainers for Integration Testing

### Status
Accepted

### Context

The project requires reliable integration testing against a real PostgreSQL database environment.

Using only H2 could hide database-specific issues because H2 behavior differs from PostgreSQL in several aspects such as SQL dialect, constraints, and transaction behavior.

### Decision

Use Testcontainers with PostgreSQL for integration and end-to-end tests.

Integration tests dynamically start isolated PostgreSQL containers during test execution.

### Consequences

#### Positive

- Tests run against a real PostgreSQL environment
- Reduces differences between development and testing environments
- Improves reliability of integration tests
- Flyway migrations are validated automatically during tests
- Better confidence in production compatibility

#### Negative

- Tests execute more slowly compared to in-memory databases
- Requires Docker availability during test execution
- Adds additional setup complexity for CI environments

---

## ADR-006: Use DTO Inheritance for Reference Types

### Status
Accepted

### Context

The application manages multiple reference types such as books, articles, web pages, and conference papers.

Each reference type shares common fields while also requiring subtype-specific attributes.

A flexible API representation strategy was needed to support polymorphic request and response handling.

### Decision

Use DTO inheritance for reference representations.

A base ReferenceDTO abstraction is extended by subtype-specific DTOs such as BookReferenceDTO and WebPageReferenceDTO.

Jackson polymorphic deserialization is used to resolve DTO subtypes through the referenceType field.

### Consequences

#### Positive

- Reuse of common reference fields
- Cleaner and more extensible DTO hierarchy
- Simplifies support for multiple reference types
- Improves API consistency
- Compatible with OpenAPI polymorphic schema generation

#### Negative

- More complex JSON serialization and deserialization
- Requires explicit subtype configuration
- Error handling becomes more complex when subtype information is missing

---

## ADR-007: Favor Explicit DTO Conversion over Automatic Mapping

### Status
Accepted

### Context

The project uses multiple DTO types and inheritance-based request/response structures.

Automatic mapping libraries such as ModelMapper can simplify DTO conversion, but inheritance-heavy and polymorphic scenarios may introduce hidden mapping behavior and reduce predictability.

A code reading analysis was conducted to evaluate ModelMapper inheritance support and runtime polymorphism limitations.

### Decision

Favor explicit DTO conversion using dedicated converter components instead of relying entirely on automatic object mapping libraries.

DTO conversion responsibilities are centralized in reusable components located in the `component` package.

### Consequences

#### Positive

- Better control over polymorphic DTO conversion
- Predictable and explicit mapping behavior
- Easier debugging and maintenance
- Reduced hidden conversion logic
- Clear separation between API and domain layers

#### Negative

- More verbose mapping code
- Additional maintenance effort for DTO converters
- Slower initial implementation compared to automatic mapping libraries

### Related Documentation

```text
docs/code-reading/modelmapper.md
```

---

## ADR-008: Standardize OpenAPI Documentation with `springdoc-openapi`

### Status
Accepted

### Context

The project exposes a REST API consumed by multiple clients and requires consistent API documentation for development, testing, and integration purposes.

Manual API documentation approaches are difficult to maintain as the API evolves.

### Decision

Use `springdoc-openapi` for automatic OpenAPI 3 specification generation and Swagger UI integration.

Endpoints should include explicit documentation annotations such as:

- `@Operation`
- `@ApiResponse`
- `@Schema`
- `@SecurityScheme`

### Consequences

#### Positive

- Standardized API documentation
- Improved developer onboarding
- Easier frontend/backend integration
- Automatic OpenAPI specification generation
- Interactive Swagger UI support

#### Negative

- Requires maintaining annotation consistency
- Additional documentation effort during development
- Complex DTO hierarchies may require manual schema adjustments

### Related Documentation

```text
docs/code-reading/springdoc-openapi.md
```

---

## ADR-009: Externalize All Secrets via Environment Variables

### Status
Accepted

### Context

During the security hardening phase (weeks 14-17), an audit found the
JWT signing secret and the initial admin password hardcoded directly
in `application-prod.properties` and `DataInitializer`, despite a
comment claiming the secret came from an environment variable. Both
values were already committed to git history, making them effectively
compromised regardless of later code changes.

### Decision

All secrets (`JWT_SECRET`, `ADMIN_INITIAL_PASSWORD`,
`CORS_ALLOWED_ORIGINS`) are read exclusively from environment
variables, with no hardcoded fallback values. The application fails
fast at startup if a required secret is missing (see `JwtUtils`'
`@PostConstruct` validation and `DataInitializer`'s explicit null
check). A secrets manager (e.g. Vault, AWS Secrets Manager) was
considered but deferred — out of scope for the current deployment
scale and infrastructure.

### Consequences

#### Positive
- No secret value can be recovered from the source code or git
  history going forward.
- Fail-fast behavior surfaces missing configuration immediately at
  startup instead of allowing insecure defaults to run silently.
- Straightforward to rotate secrets without a code change.

#### Negative
- Slightly more setup friction for new environments (a `.env` file or
  equivalent must be provisioned before the app can start).
- No centralized secret rotation or audit trail — deferred to Fase 2
  if the project's security requirements grow.

---

## ADR-010: Use `ddl-auto=validate` in Production Instead of `update`

### Status
Accepted

### Context

`application-prod.properties` had `spring.jpa.hibernate.ddl-auto=update`
while `dev` correctly used `validate`. With `update`, Hibernate could
silently alter the production schema on every startup based on entity
mappings, bypassing Flyway's version-controlled migrations entirely
and risking undocumented schema drift.

### Decision

Use `ddl-auto=validate` in both `dev` and `prod`. Hibernate only
verifies that entity mappings match the schema Flyway already
applied; it never generates or executes DDL itself. All schema
changes go exclusively through versioned Flyway migration scripts.
`test` keeps `create-drop` for speed and isolation against an
ephemeral database.

### Consequences

#### Positive
- Single source of truth for schema changes (Flyway), eliminating
  drift between what migrations record and the real database state.
- Any entity/schema mismatch fails the application at startup instead
  of silently patching production.

#### Negative
- Every entity change now requires a corresponding Flyway migration
  to be written by hand — no auto-generation shortcut during
  development against `prod`-like validation.

---

## ADR-011: Standardize Error Responses Across All Failure Paths

### Status
Accepted

### Context

Error responses were inconsistent: some exceptions returned a shared
`ErrorResponse` structure via `GlobalExceptionHandler`, but the JWT
authentication entry point (401) built its own hand-written JSON with
a different shape, and authorization denials at the security filter
level (403) had no custom handler at all, falling back to Spring
Security's default behavior. Some exceptions (`BadRequestException`,
malformed JSON, oversized uploads, RIS/BibTeX parse errors) had no
handler and fell through to a generic 500.

### Decision

All error paths — exceptions from controllers/services
(`GlobalExceptionHandler`), unauthenticated requests
(`AuthEntryPointJwt`), and authorization denials
(`AccessDeniedHandlerImpl`) — return the same `ErrorResponse`
structure (`timestamp`, `status`, `error`, `message`, `path`).
Missing handlers were added for previously-uncaught exception types,
each mapped to the correct HTTP status instead of a generic 500.

### Consequences

#### Positive
- Any API consumer can rely on one consistent error shape regardless
  of which layer rejected the request.
- Correct HTTP status codes make client-side error handling
  predictable (400 vs 401 vs 403 vs 413 vs 500).
- The generic 500 handler no longer leaks raw exception messages to
  clients; details are logged server-side only.

#### Negative
- Every new exception type introduced in the future must be
  deliberately mapped to a handler, or it will silently fall back to
  a generic 500 — this is a discipline requirement, not something
  enforced automatically.

---

## Deferred: Exhaustive OpenAPI Audit

An exhaustive audit of OpenAPI/Swagger annotation coverage and
accuracy (beyond the basic `@Operation`/`@Schema` usage already in
place, per ADR-008) was considered during the Fase 1 documentation
week but not performed, as the critical suite, CI, and minimal
hardening gates already consumed the available time this phase.

This is logged here explicitly as **Fase 2 debt** — it does not block
the closure of Fase 1.

--- 

## ADR-012: Log UserService/API DTO Coupling as Fase 2 Debt

### Status
Deferred

### Context

While evaluating week 19's entry conditions for optional package
restructuring, a real architectural inconsistency was found:
`UserService` (domain layer) accepts API-layer request/response types
directly as method parameters (`AddUserRequest`, `SignupRequest`,
`UpdateUserPasswordRequest`, `UserDTO`), unlike `ReferenceService` and
`RoleService`, which correctly depend only on domain-layer types. This
contradicts ADR-007's stated goal of isolating domain models from
external API contracts.

This is not a package-location problem (the file is already correctly
placed under `domain/service`) — it's a dependency-direction problem
that would require changing method signatures and moving conversion
responsibility to the controller/DTOConverter layer.

### Decision

Do not fix this during week 19. Week 19's restructuring PR is scoped
to physical file moves with zero functional risk ("mover archivos y
actualizar imports... sin cambios funcionales"); this fix requires
changing method contracts, which is functional-adjacent and violates
the project's own rule against mixing restructuring with fixes.

This is logged as explicit Fase 2 debt instead, aligning with the
Fase 2 preview item "evaluar si separar entity/domain tiene valor
ahora."

### Consequences

#### Positive
- Fase 1 closes without scope creep or last-minute functional risk.
- The inconsistency is documented with enough detail (affected file,
  affected methods, contrast with the other two services) to be
  picked up quickly in Fase 2.

#### Negative
- `UserService` remains inconsistent with `ReferenceService` and
  `RoleService` until Fase 2.

---


### Update (Fase 2, Semana 0)

Al evaluar el gate de entrada de Fase 2, se revisó este ADR contra
las 5 categorías que el gate permite como deuda consciente de Fase 2:
auth, OWASP, observabilidad, rate limiting, runbooks.

Este hallazgo no encaja en ninguna — es deuda de arquitectura general,
no de seguridad ni operación. Se reclasifica como **backlog general**,
candidato a Fase 3 o a cuando surja necesidad concreta de tocar
`UserService`. No entra al alcance de las 12 semanas de Fase 2.


---

## Fase 2 Entry Decision (Gate de Semana 0)

### Status
Accepted

### Context

Fase 2 no empieza porque el calendario lo diga; empieza cuando la
base de Fase 1 deja de estar abierta. El plan de Fase 2 exige
verificar, con evidencia y no con sensación, que los criterios de
entrada obligatorios se cumplen antes de arrancar el trabajo de
hardening.

### Verification performed (Semana 0)

- `mvn verify`: 140 unit tests + 21 integration tests, `BUILD SUCCESS`
  confirmado en fresco (no reusando evidencia de días anteriores).
- Último run de GitHub Actions en `dev`: verde (8+ corridas
  consecutivas en verde).
- Búsqueda explícita de las 5 señales de "Fase 1 no cerrada" listadas
  en el plan de Fase 2 — ninguna encontrada:
  - Sin `WebSecurityConfigurerAdapter`.
  - Sin `antMatchers`.
  - Sin `@CrossOrigin("*")`.
  - `permitAll()` existente es allowlist explícita
    (`/api/auth/**`, swagger, `/error`), no amplia.
  - Secrets externalizados en `application-prod.properties`
    (`${JWT_SECRET}`, `${CORS_ALLOWED_ORIGINS}`, datasource por
    variables), `ddl-auto=validate` confirmado.

### Allowed carry-over inventory

Revisados los 2 candidatos a deuda de Fase 2, evaluados contra las 5
categorías que el gate permite (auth, OWASP, observabilidad, rate
limiting, runbooks):

- **Entra a Fase 2:** auditoría exhaustiva de OpenAPI/Swagger
  (categoría auth — corresponde a la sección 1.3 del plan de Fase 2).
- **No entra a Fase 2:** ADR-012 (acoplamiento `UserService`/DTOs de
  api) — no encaja en ninguna categoría del gate. Reclasificado como
  backlog general / candidato a Fase 3 (ver actualización en
  ADR-012).

### Frozen validation baseline

El criterio único de "verde" para toda la Fase 2 queda documentado en
`docs/testing.md`, sección "Fase 2 Validation Baseline": `mvn verify`
como gate automatizado obligatorio, más el smoke test manual de 10
pasos para PRs que toquen seguridad, validación, observabilidad,
auth, import/export o logging.

### Decision

El gate de entrada a Fase 2 pasa. Fase 1 está cerrada con evidencia
verificada hoy, no arrastrada de la retrospectiva anterior. Fase 2
arranca en la Semana 1 (hardening de autorización) sobre esta base.

### Consequences

#### Positive
- Fase 2 arranca sin deuda crítica de Fase 1 disfrazada de trabajo
  nuevo.
- Existe un único criterio de validación, usable igual en local y en
  CI, sin ambigüedad sobre qué significa "pasa".

#### Negative
- Ninguna identificada — el gate pasó limpio en la primera revisión.


---

## ADR-013: Restrict Swagger/OpenAPI Exposure in Production

### Status
Accepted

### Context

`/swagger-ui.html` and `/v3/api-docs` were fully public in all
profiles, including `prod`. While no sensitive data is exposed
directly, these routes reveal the complete API surface (every
endpoint, parameter, and DTO shape), which is unnecessary exposure
for a production environment with no current external consumers
relying on public Swagger access.

### Decision

Swagger UI and OpenAPI JSON remain public (`permitAll()`) in `dev`
and `test` profiles for ease of local development, but require
authentication in `prod` — enforced via `WebSecurityConfig` checking
the active Spring profile at startup.

### Consequences

#### Positive
- API surface is no longer discoverable by anonymous requests in
  production.
- No change to local development experience (dev/test unaffected).
- Verified live: a `docker compose` startup with `prod` profile
  returns a standard `401` `ErrorResponse` for `/swagger-ui.html`
  without a JWT.

#### Negative
- Anyone needing to consult API documentation against a production
  deployment now needs a valid JWT first — acceptable trade-off given
  there are no external API consumers yet.


---

## ADR-014: Ownership Response Policy — 403 vs 404

### Status
Accepted

### Context

Weeks 1-3 of Fase 2 required picking one consistent policy for
accessing another user's resource: `403 Forbidden` (resource exists,
you can't touch it) or `404 Not Found` (resource "doesn't exist" from
your perspective) — and applying it uniformly, since mixing both
without criteria is the real risk, not the choice itself.

Auditing `ReferenceService`/`ReferenceController` and
`UserService`/`UserController` found the policy was already
implemented consistently, just never documented as an explicit
decision.

### Decision

Two distinct situations, two distinct responses:

- **Resource doesn't exist at all** (invalid/non-existent ID) →
  `ResourceNotFoundException` → `404 Not Found`.
- **Resource exists but belongs to another user** →
  `InvalidOperationException` → `403 Forbidden`.

This applies identically to references (`get`, `update`, `delete`,
`sync`, `filterOwnedReferences`) and users (`getById`).

### Consequences

#### Positive
- A `403` tells a legitimate API consumer "this exists, you're not
  allowed" — useful for debugging your own client.
- A `404` for genuinely invalid IDs avoids confirming/denying whether
  an ID exists for someone else's resources at the "not found" tier,
  though it does not fully hide existence when the ID *is* valid but
  owned by someone else (that case is explicit `403` by choice, not
  `404`) — the trade-off accepted here favors clearer client-side
  error handling over minimizing resource-existence disclosure.
- No code changes required — the existing behavior was already
  correct, just undocumented.

#### Negative
- Returning `403` instead of `404` for someone else's resource does
  technically confirm the resource ID is valid to an unauthorized
  caller. Accepted as a reasonable trade-off for this project's
  threat model; revisit if stricter existence-hiding becomes a
  requirement.

---

## ADR-015: JWT/Refresh Token Lifecycle Policy

### Status
Accepted

### Context

An audit of the JWT/refresh token lifecycle (week 4-6 of Fase 2,
`docs/security/jwt-refresh-token-audit.md`) found a critical bug: a
second login for the same user, without a prior logout, crashed with
a 500 due to `createRefreshToken()` always inserting a new row
against a `user_id UNIQUE` constraint. It also found no rotation on
refresh token use, meaning a leaked refresh token stayed valid for
its full 10-day lifetime with no mitigation.

### Decision

- **One refresh token per user, enforced by upsert.** `createRefreshToken()`
  reuses the existing row for the user (new token value, new expiry)
  instead of inserting a duplicate. A second login simply issues a
  fresh session instead of erroring.
- **Rotation on every refresh.** `POST /api/auth/refresh-token`
  invalidates the token it was called with and issues a new one.
  The old token cannot be reused afterward.
- **Logout deletes the refresh token.** `POST /api/auth/logout`
  (already required authentication since #150) removes the user's
  refresh token via `deleteByUserId`.
- JWT expiration: 1 hour. Refresh token expiration: 10 days. Same
  across all profiles.

### Consequences

#### Positive
- A common, legitimate flow (logging in from a second device/tab)
  no longer crashes the app.
- A leaked refresh token has a much smaller window of usability
  (until its next legitimate use by the real owner, not its full
  10-day lifetime).

#### Negative
- Logging in from a second device invalidates the first device's
  session for the purposes of *creating a new* refresh token — the
  first device's existing JWT still works until it expires (1 hour),
  but its refresh token is replaced, so it can't get a new JWT after
  that without logging in again. This is a reasonable trade-off for
  a personal-scale project; revisit if multi-device concurrent
  sessions become a real requirement.


---

## ADR-016: Per-Profile CORS Configuration

### Status
Accepted

### Context

Fase 2's week 4-6 plan required confirming CORS has an explicit,
minimal configuration per profile, and that no wildcard origin is
combined with credentials — a common CORS misconfiguration.

### Decision

CORS is centralized in `CorsConfig`/`CorsProperties` (since Fase 1),
reading `cors.allowed-origins` per profile:
- `dev`: `http://localhost:3000` (the local frontend dev server).
- `prod`: `${CORS_ALLOWED_ORIGINS}`, externalized via environment
  variable since ADR-009 — never hardcoded, never a wildcard.
- `test`/`integration`: no explicit value; `allowedOrigins` resolves
  to `null` (equivalent to "no origins configured"), which is
  acceptable since integration tests don't exercise cross-origin
  behavior.

`allowCredentials` is `true` across all profiles, but since
`allowedOrigins` is never `*` in any profile, the unsafe
wildcard-plus-credentials combination cannot occur.

### Consequences

#### Positive
- Confirmed no code change was needed — the Fase 1 centralization
  work (ADR-009) already resulted in a safe, per-profile CORS setup.
- Explicit documentation closes out this item of the Fase 2 plan
  with evidence, not assumption.

#### Negative
- None identified.


---

## ADR-017: OWASP Hardening for Import/Export — Server-Generated Filenames Only

### Status
Accepted

### Context

Fase 1 (#137) fixed a path traversal vulnerability in export by
sanitizing the client-supplied `fileName` before using it to build a
filesystem path. Fase 2's stricter OWASP standard for week 4-6 goes
further: the server should never use any client-supplied value to
construct a filesystem path, sanitized or not.

### Decision

- **Import**: the physical temp filename is generated entirely by the
  server (`UUID.randomUUID()` + validated extension). The client's
  original filename is read only to validate its extension against
  an allowlist (`.ris`, `.bib`, `.bibtex`) — never used in the path.
- **Export**: the physical temp filename is `UUID.randomUUID()`
  alone. The client-supplied `fileName` is still sanitized (per
  #137) and used, but only for the `Content-Disposition` response
  header — response metadata the browser uses to name the downloaded
  file, not a filesystem path.
- Temp files (both import and export) are always deleted via
  try/finally, regardless of success or failure (since #137).

### Consequences

#### Positive
- No client-supplied string can influence any filesystem path,
  closing the category of risk entirely rather than relying on
  sanitization alone.
- Covered by `ImportExportSecurityIntegrationTest`: valid file
  accepted, empty file rejected, malicious filename never escapes
  the uploads directory, temp files cleaned up on failure.

#### Negative
- None identified — the download filename shown to the user is
  unaffected; only the internal, temporary on-disk name changed.
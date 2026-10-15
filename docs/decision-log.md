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
- No centralized secret rotation or audit trail — deferred to Phase 2
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
place, per ADR-008) was considered during the Phase 1 documentation
week but not performed, as the critical suite, CI, and minimal
hardening gates already consumed the available time this phase.

This is logged here explicitly as **Phase 2 debt** — it does not block
the closure of Phase 1.

--- 

## ADR-012: Log UserService/API DTO Coupling as Phase 2 Debt

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
to physical file moves with zero functional risk ("move files and
update imports... no functional changes"); this fix requires
changing method contracts, which is functional-adjacent and violates
the project's own rule against mixing restructuring with fixes.

This is logged as explicit Phase 2 debt instead, aligning with the
Phase 2 preview item "evaluate whether separating entity/domain is
worthwhile now."

### Consequences

#### Positive
- Phase 1 closes without scope creep or last-minute functional risk.
- The inconsistency is documented with enough detail (affected file,
  affected methods, contrast with the other two services) to be
  picked up quickly in Phase 2.

#### Negative
- `UserService` remains inconsistent with `ReferenceService` and
  `RoleService` until Phase 2.

---


### Update (Phase 2, Week 0)

When evaluating the Phase 2 entry gate, this ADR was reviewed against
the 5 categories the gate allows as conscious Phase 2 debt:
auth, OWASP, observability, rate limiting, runbooks.

This finding fits none of them — it is general architecture debt,
not security or operations. It is reclassified as **general backlog**,
a candidate for Phase 3 or for when a concrete need to touch
`UserService` arises. It does not enter the scope of the 12 weeks of Phase 2.


---

## Phase 2 Entry Decision (Week 0 Gate)

### Status
Accepted

### Context

Phase 2 does not start because the calendar says so; it starts when the
Phase 1 base stops being open. The Phase 2 plan requires verifying,
with evidence and not with a feeling, that the mandatory entry
criteria are met before starting the hardening work.

### Verification performed (Week 0)

- `mvn verify`: 140 unit tests + 21 integration tests, `BUILD SUCCESS`
  confirmed fresh (not reusing evidence from earlier days).
- Last GitHub Actions run on `dev`: green (8+ consecutive
  green runs).
- Explicit search for the 5 "Phase 1 not closed" signals listed
  in the Phase 2 plan — none found:
  - No `WebSecurityConfigurerAdapter`.
  - No `antMatchers`.
  - No `@CrossOrigin("*")`.
  - The existing `permitAll()` is an explicit allowlist
    (`/api/auth/**`, swagger, `/error`), not a broad one.
  - Secrets externalized in `application-prod.properties`
    (`${JWT_SECRET}`, `${CORS_ALLOWED_ORIGINS}`, datasource through
    variables), `ddl-auto=validate` confirmed.

### Allowed carry-over inventory

Reviewed the 2 candidates for Phase 2 debt, evaluated against the 5
categories the gate allows (auth, OWASP, observability, rate
limiting, runbooks):

- **Enters Phase 2:** exhaustive audit of OpenAPI/Swagger
  (auth category — corresponds to section 1.3 of the Phase 2 plan).
- **Does not enter Phase 2:** ADR-012 (`UserService`/api DTO
  coupling) — it fits no gate category. Reclassified as
  general backlog / candidate for Phase 3 (see the update in
  ADR-012).

### Frozen validation baseline

The single "green" criterion for all of Phase 2 is documented in
`docs/testing.md`, section "Fase 2 Validation Baseline": `mvn verify`
as the mandatory automated gate, plus the manual 10-step smoke test
for PRs that touch security, validation, observability,
auth, import/export or logging.

### Decision

The Phase 2 entry gate passes. Phase 1 is closed with evidence
verified today, not carried over from the earlier retrospective. Phase 2
starts in Week 1 (authorization hardening) on this base.

### Consequences

#### Positive
- Phase 2 starts without critical Phase 1 debt disguised as new
  work.
- There is a single validation criterion, usable the same way locally and in
  CI, with no ambiguity about what "passes" means.

#### Negative
- None identified — the gate passed cleanly on the first review.


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

Weeks 1-3 of Phase 2 required picking one consistent policy for
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

An audit of the JWT/refresh token lifecycle (week 4-6 of Phase 2,
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

Phase 2's week 4-6 plan required confirming CORS has an explicit,
minimal configuration per profile, and that no wildcard origin is
combined with credentials — a common CORS misconfiguration.

### Decision

CORS is centralized in `CorsConfig`/`CorsProperties` (since Phase 1),
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
- Confirmed no code change was needed — the Phase 1 centralization
  work (ADR-009) already resulted in a safe, per-profile CORS setup.
- Explicit documentation closes out this item of the Phase 2 plan
  with evidence, not assumption.

#### Negative
- None identified.


---

## ADR-017: OWASP Hardening for Import/Export — Server-Generated Filenames Only

### Status
Accepted

### Context

Phase 1 (#137) fixed a path traversal vulnerability in export by
sanitizing the client-supplied `fileName` before using it to build a
filesystem path. Phase 2's stricter OWASP standard for week 4-6 goes
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

---

## ADR-018: Rate Limiting Strategy for Auth Endpoints

### Status
Accepted

### Context

`/api/auth/signin` and `/api/auth/refresh-token` were unprotected
against brute force — no limit on repeated attempts.

### Decision

Added in-memory rate limiting via Bucket4j, keyed by client IP,
applied only to `/api/auth/signin` and `/api/auth/refresh-token`.
Threshold and refill window are configurable per profile
(`codexrm.ratelimit.auth.*`): 5 attempts/minute in `prod`, 100/minute
in `dev` (so local development isn't hindered), and 1000/minute in
`test`/`integration` (so the test suite's many rapid signin calls
across different scenarios don't trip the limiter against each
other, since they all share the same loopback IP in a test
environment).

`AuthRateLimitFilter` is deliberately **not** a `@Component` —
it's constructed manually as a `@Bean` inside `WebSecurityConfig`,
following the same pattern as `AuthTokenFilter`. This was a real
lesson learned during implementation: making it `@Component` caused
`@WebMvcTest`-based controller tests to auto-detect it as a global
filter, and mocking it with `@MockBean` produced a no-op stub that
never called `filterChain.doFilter(...)`, silently blocking every
request in every controller test (`AuthControllerTest`,
`ReferenceControllerTest`, `UserControllerTest` all failed with
empty 200 responses). Avoiding `@Component` sidesteps this entirely.

An in-memory, single-instance strategy is adequate for this
project's current scale — no distributed store (Redis, etc.) is
needed. If the app ever runs across multiple instances, each
instance would track its own limit independently, which would need
to move to a shared store to remain effective.

### Consequences

#### Positive
- Brute-force attempts against login/refresh are throttled with a
  clear, standard `429` response using the existing `ErrorResponse`
  structure.
- Threshold is configurable per environment without code changes.
- Verified live: 6 rapid failed signin attempts in `dev` (limit
  temporarily lowered to 5) returned 429 on the 6th, with the
  correlationId (from ADR at #161) still present.

#### Negative
- Rate limiting by IP means users behind a shared IP (NAT, corporate
  network) share the same bucket — acceptable trade-off at this
  scale, revisit if it becomes a real complaint.
- Not effective across multiple app instances without a shared store
  — noted as a known limitation, not a current requirement.

### Update (Phase 3, Week 0)

Behind a load balancer, every request arrives with the balancer's address
instead of the client's, so the per-IP limit would treat all users as a
single one and would stop protecting without giving any signal. The
`X-Forwarded-For` policy remains as Phase 3 debt (task 5.5 of the plan).
Also, with several instances each one keeps its own counter, as the
consequences section already stated.

---

## ADR-019: Phase 2 Closure and Handoff to Phase 3

### Status
Accepted

### Context

Phase 2 (12 weeks: entry gate + authorization hardening + JWT/CORS/OWASP + observability/rate limiting/quality + runbooks/post-mortem) requires an explicit closure decision, backed by verified evidence — not just the feeling that "it's done", just as was done when closing Phase 1.

### Verification performed (Week 12)

The 10 items of the closure checklist were verified with fresh evidence, not from memory:

- `mvn verify`: `BUILD SUCCESS` confirmed today (145+ unit tests, 35+ integration tests).
- Authorization: `git log` on `WebSecurityConfig.java` confirms the last change is the rate limiting one (week 7-9) — the authorization matrix is still valid, with no drift.
- OWASP import/export: `git log` on `ReferenceController.java` confirms the server-generated file name fix (week 4-6) was not overwritten by later changes.
- CORS: confirmed `http://localhost:3000` (dev) and `${CORS_ALLOWED_ORIGINS}` (prod) unchanged, with no wildcard in any profile.
- Structured logs: confirmed JSON with `correlationId` when starting the server.
- Rate limiting: `RateLimitIntegrationTest` passing, `429` confirmed live in week 7-9.
- Load baseline: `docs/performance/load-baseline.md` exists with real results for 2 scenarios.
- Runbooks: all 3 (`auth-401-403.md`, `refresh-token-failed.md`, `import-export-rejected.md`) exist in `docs/runbooks/`, verified against real behavior live during weeks 10-11.

### Security findings resolved during Phase 2

4 real security findings were found and fixed during this phase; none was postponed:

1. Double login crashing with a 500 (week 4-6) — fixed with upsert + refresh token rotation.
2. Full JWT logged in plain text (week 7-9) — fixed; now only 8 characters at debug level.
3. Path traversal in export (deepened in week 4-6, on top of the Phase 1 base fix #137) — file names are 100% server-generated.
4. `AUDITOR` incorrectly blocked and `logout` without real authentication (week 1-3) — both fixed.

### Known debt carried forward (not a security vulnerability)

- **ADR-012**: `UserService` accepts DTOs from the `api` layer directly, violating the layer boundary that `ReferenceService`/`RoleService` respect. Explicitly evaluated in week 0 of Phase 2 against the gate's 5 categories (auth, OWASP, observability, rate limiting, runbooks) — it fits none of them. It is general architecture debt, not security or operations. It remains a candidate for Phase 3, or for when a concrete need to touch `UserService` arises.

### Decision

The Phase 2 closure gate passes. The 10 checklist items pass with verified evidence. No known critical vulnerability remains without a fix — the only pending debt (ADR-012) is general architecture, explicitly outside the scope of Phase 2 since its own entry gate.

Phase 3 can begin on this base.

### Consequences

#### Positive
- Phase 3 starts without hidden or unresolved security debt.
- The pattern of verifying with evidence (not memory) held through all 12 weeks, including the closure itself.

#### Negative
- None identified — the gate passed cleanly.

---

## ADR-020: Phase 3 Entry — Verification Gate

### Status
Accepted

### Context

Phase 3 builds infrastructure (image, Terraform, AWS) on top of the
application that closed Phase 2. Deploying on a base with secrets in
the repository or routes without authorization tests would expose
those problems instead of resolving them, so before writing
infrastructure it was verified with fresh evidence (2026-10-09 to
2026-10-11) that the base is still hardened.

### Decision

The entry gate was evaluated against the four criteria defined
for week 0:

1. **No critical security risk open.** `mvn verify` ends in
   `BUILD SUCCESS`: 142 unit tests and 36 integration tests, with no
   failures. The most recent commits of `WebSecurityConfig` (rate limiting, #163)
   and `ReferenceController` (event catalog, #162) are those of
   weeks 7-9 of Phase 2: there are no changes after the closure. The fix for
   server-generated file names (#159) is still in place. The 5 Phase 2
   closure documents exist.
2. **Startup with environment variables only.** The `.jar` starts with
   the `prod` profile receiving everything through variables, fails without
   `JWT_SECRET` and fails with a secret shorter than 64 characters.
   List of variables in `docs/infra/environment-variables.md`.
3. **Explicit list of local disk state.** Inventory in
   `docs/infra/local-filesystem-inventory.md`: the only use is the
   per-request temporary file under `java.io.tmpdir/tempUpload`,
   which is deleted in `finally`.
4. **Single "green" criterion.** Defined in
   `docs/infra/green-criterion.md` (`mvn clean verify` plus a manual
   smoke test of the `.jar`).

Findings fixed during the gate:

- `spring-boot-maven-plugin` had no version and Maven resolved
  `4.2.0-M2` (a milestone version). It was pinned to `3.2.5`, the same as
  the dependency BOM.
- The `import-export-rejected.md` runbook still listed the import/export
  events as pending; it was replaced with the four real
  events and the event catalog was aligned.

Baseline correction: ADR-019 and the Phase 2 retrospective
cite "145+ unit / 35+ integration". The actual reports give **142
unit and 36 integration tests**; no deleted tests were found, so
the earlier figure was imprecise. This entry sets the Phase 3
baseline.

The Phase 3 entry gate passes. Weeks 1-2 can
begin.

### Conscious debts entering Phase 3

| Debt | Origin | Where it is resolved |
|---|---|---|
| `X-Forwarded-For` policy for rate limiting behind a load balancer | Phase 3 plan note (section 5.5); it was not recorded in ADR-018 | Task 5.5 |
| In-memory, per-instance rate limiting: with several instances, each one counts separately (limitation already described in ADR-018) | ADR-018 | No task assigned; the plan contemplates deployments with several instances, so it must be decided when their number is set |
| `tempUpload` and its dependence on the local temporary path | Inventory of this gate | Task 3.5 |
| Implicit Spring Boot spooling for multipart uploads (not verified) | Inventory of this gate | Review in task 3.5 |
| `/actuator/health` documented in the README but without an Actuator dependency | Green criterion | Weeks 1-2 |
| CI does not run `package`: it does not build or repackage the `.jar` | Green criterion | When the pipeline is extended |
| Literal development values: PostgreSQL user and password and `dev` `jwtSecret`, `test` `jwtSecret` | Properties review | Accepted; the `dev` `jwtSecret` was verified to differ from the real one |
| Coupling of `UserService` with `api` layer DTOs (ADR-012) | Phase 1, carried over from Phase 2 | No task assigned in the Phase 3 plan; still open |
| `mvnw` exists but is not used in CI or in the instructions | Green criterion | Future decision |

### Consequences

#### Positive
- Phase 3 starts with recent evidence, not with the memory of an
  earlier closure.
- The debts that enter are named, with origin and destination.
- Two real drifts were fixed (unpinned plugin, outdated runbook)
  before they could affect the deployment.

#### Negative
- The Phase 2 retrospective and ADR-019 keep the imprecise test
  figure; the correction lives only in this ADR.
- Several debts depend on later tasks and are not closed until
  then.

---

## ADR-021: English as the Working Language

### Status
Accepted

### Context

Project artifacts were written in a mix of English and Spanish. Code,
identifiers, commit messages and ADR-001 to ADR-018 are in English, while
some documents (ADR-019, ADR-020, runbooks, retrospectives and parts of
`docs/infra/`) are in Spanish. The mix makes the repository harder to read
and to share.

### Decision

From 2026-10-15, all work is written in English: code, comments, log and
error messages, documentation, ADRs, milestones, issues, pull requests and
commit messages.

Existing Spanish text in `codexrm-server` is translated as part of the
milestone "Housekeeping: English documentation and accuracy". Translations
preserve the meaning and facts of the original; historical figures are not
corrected during translation.

GitHub history (closed issues, milestones, pull requests and past commit
messages) is not rewritten.

### Consequences

#### Positive
- One language across the repository once the translation is complete.
- New contributors and reviewers can read every document.

#### Negative
- The repository is bilingual until the milestone is finished.
- Translation effort, and the risk of changing the meaning of past
  decisions; mitigated by translating without altering facts.
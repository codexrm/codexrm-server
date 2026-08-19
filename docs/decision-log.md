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


### Update (Fase 2, Semana 0)

Al evaluar el gate de entrada de Fase 2, se revisó este ADR contra
las 5 categorías que el gate permite como deuda consciente de Fase 2:
auth, OWASP, observabilidad, rate limiting, runbooks.

Este hallazgo no encaja en ninguna — es deuda de arquitectura general,
no de seguridad ni operación. Se reclasifica como **backlog general**,
candidato a Fase 3 o a cuando surja necesidad concreta de tocar
`UserService`. No entra al alcance de las 12 semanas de Fase 2.
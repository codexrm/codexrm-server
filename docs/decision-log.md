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

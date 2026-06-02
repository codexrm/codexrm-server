# Integration Testing Strategy

## Overview

This project includes a critical integration test suite designed to validate
application behavior against a real PostgreSQL database using Testcontainers.

The suite focuses on production-like validation that cannot be reliably covered
by unit tests or H2-based database tests.

---

## Critical Suite Responsibilities

The critical integration suite validates production-sensitive behavior that
cannot be fully verified through unit tests or H2-based validation.

The suite is responsible for:

- authentication and authorization validation
- reference management validation
- synchronization workflow validation
- Flyway migration validation
- OpenAPI availability validation
- PostgreSQL compatibility verification

These tests provide confidence that the application behaves correctly in a
production-like environment.

---

## Authentication Flows

Covered by:

- AuthIntegrationTest

Validated scenarios:

- user registration
- user authentication
- duplicate signup rejection
- invalid credentials handling
- JWT-protected endpoint access
- unauthorized access handling

---

## Reference Flows

Covered by:

- ReferenceFlowIntegrationTest
- ReferenceAuthorizationIntegrationTest

Validated scenarios:

- reference creation
- reference retrieval
- ownership validation
- user isolation
- protected resource access
- reference deletion

---

## Synchronization Coverage

Covered by:

- ReferenceSynchronizationIntegrationTest

Validated scenarios:

- synchronized creation
- synchronized update
- synchronized deletion

Purpose:

Ensure synchronization operations remain consistent and predictable.

---

## Flyway Validation

Flyway migrations are validated during integration test startup.

Validated aspects:

- schema creation
- migration execution
- startup consistency
- PostgreSQL compatibility

Purpose:

Detect migration issues before deployment.

---

## OpenAPI Validation

Covered by:

- OpenApiIntegrationTest

Validated scenarios:

- OpenAPI specification exposure
- Swagger UI availability

---

## PostgreSQL and Testcontainers Strategy

The integration suite uses PostgreSQL through Testcontainers.

Benefits:

- production-like database behavior
- real SQL execution
- real transaction handling
- migration validation
- constraint validation

This approach reduces discrepancies between test and production environments.

The suite uses a PostgreSQL Testcontainer defined in BaseIntegrationTest.

---

## H2 Limitations

H2 is useful for fast testing but differs from PostgreSQL in several areas:

- SQL dialect behavior
- migration compatibility
- transaction handling
- constraint validation
- PostgreSQL-specific features

For this reason, critical integration tests rely on PostgreSQL rather than H2.

---

## Running the Test Suite

Run all tests:

```bash
mvn test
```

Run a specific integration test:

```bash
mvn -Dtest=ReferenceSynchronizationIntegrationTest test
```

### Requirements:

- Docker installed and running
- Internet access for initial container image download


### Expected result:
```text
BUILD SUCCESS
```

---
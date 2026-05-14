# Architecture Overview

This document describes the backend architecture of CodexRM Server.

The project follows a layered architecture using Spring Boot, separating API exposure, business logic, persistence, and infrastructure concerns.

The main architectural goals are:

- Clear separation of responsibilities
- Maintainability
- Scalability
- Testability
- Infrastructure isolation

---

## High-Level Architecture

CodexRM Server is organized into multiple layers, each with a specific responsibility.

The application follows a request flow where HTTP requests enter through the API layer, pass through the domain layer, interact with the infrastructure layer, and finally persist data in PostgreSQL.

### Main Layers

| Layer | Responsibility |
|---|---|
| API | Controllers, request/response DTOs, validation |
| Domain | Business rules and application services |
| Infrastructure | Persistence, security, configuration |
| Database | PostgreSQL relational database |

---

## Component Diagram

```text
+-------------------+
|      Client       |
| Web / Desktop App |
+---------+---------+
          |
          v
+-------------------+
|    API Layer      |
| Controllers + DTO |
+---------+---------+
          |
          v
+-------------------+
|   Domain Layer    |
| Services + Rules  |
+---------+---------+
          |
          v
+-------------------+
| Infrastructure    |
| Repository + JWT  |
+---------+---------+
          |
          v
+-------------------+
|   PostgreSQL DB   |
+-------------------+
```

---

## Request Lifecycle

This section describes how a typical HTTP request flows through the application.

### Example: Create Reference

Endpoint:

```text
POST /api/references
```

### Request Flow

1. The client sends an HTTP request to the API.

2. The request is received by a controller in the `api.controller` package.

3. Request payload validation is executed using Jakarta Validation annotations.

4. The controller delegates business operations to the service layer.

5. The service layer applies business rules and coordinates the operation.

6. DTOs are converted into domain entities.

7. The repository layer persists data using Spring Data JPA.

8. Hibernate communicates with PostgreSQL.

9. The persisted entity is converted back into a response DTO.

10. The API returns an HTTP response to the client.

### Security Flow

Protected endpoints use JWT authentication.

The authentication filter:

- Extracts the JWT token from the Authorization header
- Validates the token
- Loads authenticated user details
- Populates the Spring Security context

---

## Package Structure

The backend project is organized using a layered package structure.

```text
src/main/java/io/github/codexrm/server

├── api
│   ├── controller
│   └── dto
│       ├── request
│       └── response
│
├── component
│
├── domain
│   ├── enums
│   ├── model
│   └── service
│
└── infrastructure
    ├── config
    ├── exception
    ├── persistence
    │   └── repository
    └── security
        ├── jwt
        └── services
```

### Package Responsibilities

#### api

Contains REST controllers and DTOs used for HTTP communication.

#### domain

Contains business entities, enums, and application services.

#### infrastructure

Contains persistence implementation, security configuration, JWT utilities, and technical concerns.

#### component

Contains shared reusable components and utilities.

---

## Layer Separation

The project separates business logic from framework-specific concerns.

### API Layer

Responsible for:

- HTTP request handling
- Validation
- DTO serialization/deserialization
- Response formatting

The API layer should not contain business logic.

---

### Domain Layer

Responsible for:

- Business rules
- Application services
- Core domain models
- Domain behavior

This layer represents the core logic of the application.

---

### Infrastructure Layer

Responsible for:

- Database access
- Spring Security configuration
- JWT processing
- External integrations
- Technical configuration

This layer supports the domain and API layers.

---

### Database Layer

PostgreSQL is used as the primary relational database.

Database schema changes are managed using Flyway migrations.

---

## Architecture Goals

The current architecture was designed to:

- Improve maintainability
- Simplify testing
- Reduce coupling
- Support future scalability
- Improve code readability
- Separate technical concerns from business logic

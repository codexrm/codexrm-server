# CodexRM Server

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)

Backend REST API for managing bibliographic references and citations.

CodexRM provides authentication, reference management, synchronization support, and import/export capabilities for bibliographic references across multiple clients.

The system uses JWT-based authentication and a Last Write Wins synchronization strategy for conflict resolution.

---

## Documentation

- [CI Validation Workflow](docs/ci-validation.md)

---

## Features

### Reference Management

- Create and manage bibliographic references
- Retrieve references by user
- Pagination, sorting and filtering support

### Synchronization

- Multi-client synchronization support
- Last Write Wins conflict resolution strategy

### Import / Export

- Import references from RIS and BibTeX files
- Export references to RIS and BibTeX formats

---

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security + JWT
- PostgreSQL
- Flyway
- Docker & Docker Compose
- OpenAPI / Swagger
- JUnit 5
- Testcontainers
- Maven

---

## Project Structure

The project follows a layered architecture with clear separation of responsibilities.

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
### Layer Responsibilities

| Layer | Responsibility |
|---|---|
| api | REST controllers and DTOs |
| domain | Business rules and domain models |
| infrastructure | Persistence, security and framework configuration |
| component | Shared converters and helper components |

---

## Architecture Overview

CodexRM follows a layered architecture designed to separate API exposure, business rules, persistence, and infrastructure concerns.

Main architectural goals:

- maintainability
- clear separation of responsibilities
- scalability
- testability
- framework isolation

Key architectural decisions:

- DTO-based API layer
- JWT authentication with Spring Security
- DTOConverter components for explicit DTO mapping
- Flyway-managed database migrations
- OpenAPI-first API documentation

---

## Additional Documentation

- [CI Validation Workflow](docs/ci-validation.md)
- [CI Strategy for Multi-Repository Architecture](docs/ci-strategy.md)

---

## Running the Project

### Prerequisites

Before running the project, make sure you have installed:

- Java 21
- Maven 3.9+
- Docker
- Docker Compose

---

## Run Locally

### 1. Clone the repository

```bash
git clone <repository-url>
cd codexrm-server
```

### 2. Start PostgreSQL with Docker

```bash
docker compose up db
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

---

## Run with Docker Compose

### Build the project

```bash
mvn clean package
```

This generates:

```text
target/server-0.0.1-SNAPSHOT.jar
```

### Start all services

```bash
docker compose up --build
```

Services started:

| Service | Description | Port |
|---|---|------|
| codexrm-server | Spring Boot REST API | 8081 |
| codexrm-db | PostgreSQL Database | 5433 |

---

## Environment Variables

| Variable | Description | Default Value                     |
|---|---|-----------------------------------|
| SPRING_PROFILES_ACTIVE | Active Spring profile | prod                              |
| SPRING_DATASOURCE_URL | PostgreSQL connection URL | jdbc:postgresql://db:5432/codexrm |
| SPRING_DATASOURCE_USERNAME | Database username | codexrm                           |
| SPRING_DATASOURCE_PASSWORD | Database password | codexrm                           |
| JWT_SECRET | Secret key used to sign JWT tokens (min. 64 chars) | *(required, no default)* |
| CORS_ALLOWED_ORIGINS | Comma-separated list of allowed CORS origins | *(required, no default)* |
| ADMIN_INITIAL_PASSWORD | Password for the initial admin user created on first startup | *(required, no default)* |

---

## Running Tests

Run all tests:

```bash
mvn test
```

Run a specific test class:

```bash
mvn test -Dtest=ReferenceFlowIntegrationTest
```

The project includes:

- Unit tests
- Repository tests with H2
- Integration tests with Testcontainers + PostgreSQL
- OpenAPI contract tests

---

## API Documentation

The API is documented using OpenAPI 3.0 and Swagger UI.

### Swagger UI

```text
http://localhost:8081/swagger-ui.html
```

### OpenAPI JSON

```text
http://localhost:8081/v3/api-docs
```

### OpenAPI YAML

```text
http://localhost:8081/v3/api-docs.yaml
```

---

## Importing into Postman

1. Open Postman
2. Click **Import**
3. Select **Link**
4. Paste:

```text
http://localhost:8081/v3/api-docs
```

Postman will automatically generate a collection with all endpoints.

---

## Useful Development Commands

Build the project:

```bash
mvn clean package
```

Run tests:

```bash
mvn test
```

Run Spring Boot locally:

```bash
mvn spring-boot:run
```

Start Docker services:

```bash
docker compose up --build
```

Stop Docker services:

```bash
docker compose down
```

Run integration tests only:

```bash
mvn test -Dtest="*IntegrationTest"
```
---

## Health Check

```text
http://localhost:8081/actuator/health
```
---

## Future Improvements

- Improve API documentation examples
- Add CI/CD pipeline automation
- Increase test coverage
- Add observability and metrics
- Improve synchronization conflict resolution

---
## Author

Backend API developed for the CodexRM bibliographic reference management system.
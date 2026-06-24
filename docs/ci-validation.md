# CI Validation Workflow

## Purpose

The CI pipeline validates code quality and application stability before changes are merged.

The workflow executes:

1. Project build
2. Unit tests
3. Critical integration tests

A Pull Request should only be merged after all CI checks pass.

## Workflow Triggers

The GitHub Actions workflow runs on:

### Push

* main
* develop
* feature/**
* test/**
* docs/**
* ci/**

### Pull Requests

* Any pull request targeting the repository

## Local Validation

Developers should run the same validation steps locally before creating a Pull Request.

### Prerequisites

* Java 21
* Maven
* PostgreSQL 15 or compatible version

### Database Configuration

Create a local database with:

* Database: codexrm_test
* Username: postgres
* Password: postgres

### Run Unit Tests

```bash
mvn clean test
```

### Run Integration Tests

```bash
mvn failsafe:integration-test failsafe:verify
```

Required environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/codexrm_test
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
SPRING_PROFILES_ACTIVE=test,integration
```

## CI Gate

All CI checks must pass before a Pull Request is approved and merged.

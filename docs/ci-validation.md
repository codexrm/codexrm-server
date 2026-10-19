# CI Validation Workflow

## Purpose

The CI pipeline validates code quality and application stability before changes are merged.

The workflow (`.github/workflows/build.yml`, named "Build and Test") executes:

1. Project build and unit tests (`mvn clean test`)
2. Critical integration tests (`mvn failsafe:integration-test failsafe:verify`)
3. Upload of the Surefire and Failsafe reports as an artifact (always, even if a step fails)

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

* Any pull request, regardless of the target branch

### Known gap

The integration branch of this repository is `dev`, which is not in the push list (neither are the `chore/**`, `fix/**` and `refactor/**` prefixes). Pull requests are still validated, because the `pull_request` trigger has no filters, but a push to `dev` (for example, the merge of a pull request) does not start a run.

## What the Workflow Does

* Runs on `ubuntu-latest` with Java 21 (Temurin) and the Maven cache.
* Writes `~/.m2/settings.xml` with credentials (the `READ_PACKAGES_TOKEN` secret) for the three GitHub Packages repositories (server ids `github`, `github-jris` and `github-eilibrary`), so Maven can download the parent POM, EILibrary and jRis. See [ci-strategy.md](ci-strategy.md).
* `mvn clean test` builds the project and runs the unit and repository tests (Surefire, which excludes `*IntegrationTest`).
* `mvn failsafe:integration-test failsafe:verify` runs the integration tests (Failsafe, which includes `*IntegrationTest`) with `SPRING_PROFILES_ACTIVE=test,integration`.
* It never runs the `package` phase. Failsafe is configured to use `target/classes`, so the integration tests run against compiled classes, not against the `.jar`. A packaging problem is therefore not detected by CI; `mvn clean verify` does detect it (see [infra/green-criterion.md](infra/green-criterion.md)).
* The workflow also starts a `postgres:15` service and sets the `SPRING_DATASOURCE_*` variables, but the integration tests do not use them: `BaseIntegrationTest` starts its own PostgreSQL 16 container with Testcontainers and overrides the datasource properties with `@DynamicPropertySource`.

## Local Validation

Developers should run the same validation steps locally before creating a Pull Request.

### Prerequisites

* Java 21
* Maven
* Docker (Testcontainers starts PostgreSQL for the integration tests; no local database is needed)
* Credentials for GitHub Packages in `~/.m2/settings.xml`, with the same server ids as CI, if the internal dependencies are not already in your local Maven repository

### Run Unit Tests

```bash
mvn clean test
```

No Docker required.

### Run Integration Tests

```bash
mvn failsafe:integration-test failsafe:verify
```

Run it after `mvn clean test`, so the classes are compiled. Docker must be running.

### Run the Full Local Gate

```bash
mvn clean verify
```

Runs everything above and also packages the `.jar`, which CI does not do.

## CI Gate

All CI checks must pass before a Pull Request is approved and merged.
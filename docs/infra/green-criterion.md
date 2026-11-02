# "It's green" criterion for Phase 3

Current version: 2026-11-02 (weeks 1-2). It is extended week by week,
as Docker, Actuator and Terraform come in. It starts from the 13-step
list in the Phase 3 plan.

## Current criterion

### Part A: build and tests (plan step 1)

From `codexrm-server`, with Docker Desktop open (Testcontainers
starts PostgreSQL):

    mvn clean verify

It is green if it ends in `BUILD SUCCESS`, with no failures, with at
least 142 unit tests and 42 integration tests (baseline after the
Actuator health tests of weeks 1-2).

### Part B: smoke test of the production image (plan steps 2, 3, 4, 9 and 11)

The image is built from the repository only. Maven needs a
`settings.xml` with a token that can read GitHub Packages (BuildKit
secret, never copied into the image). The variables for the `prod`
profile are in `docs/infra/environment-variables.md`.

1. Build the image, tagged with the short commit SHA (plan step 2):

        $sha = git rev-parse --short HEAD
        docker build --secret id=maven_settings,src=$env:USERPROFILE\.m2\settings.xml --build-arg GIT_SHA=$sha -t codexrm-server:$sha .

2. Start a PostgreSQL container and the image on the same Docker
   network, with the `prod` profile and the minimal variables
   (plan step 3). Use test-only values, and a `JWT_SECRET` of at least
   64 characters.
3. `GET /actuator/health` must return `200` with `{"status":"UP"}`,
   from inside the container and from the host through the published
   port (plan step 4). `docker ps` must show `(healthy)`.
4. `POST /api/auth/signin` with a test user must return `200` with a
   token (plan step 9). The rate limit in `prod` is 5 attempts per
   minute per IP.
5. In the container logs, every line must be JSON, and those of a
   request carry `correlationId` (plan step 11).

Optional checks, done in #215 and recorded in ADR-024: the process
does not run as root, the image has no JDK or sources, and the
container starts on an internal Docker network with no internet access.

## Relationship with CI

CI runs `mvn clean test` and
`mvn failsafe:integration-test failsafe:verify`. These are the same
tests as Part A, but CI does not run the `package` phase and does not
build the Docker image. A packaging or Dockerfile problem is not
detected by CI, but it is detected by `mvn clean verify` and by
Part B. Aligning CI with this criterion is a pending task.

## Plan steps that do not apply yet

| Step | What it checks | Why it does not apply today |
|---|---|---|
| 5-8, 12 | `terraform fmt`, `validate`, `plan`, `apply`, `destroy` | Terraform comes in during weeks 5-8 |
| 10 | The import goes to S3 | It is task 3.5 |
| 13 | Repeat against real AWS | Real AWS window (weeks 11-13) |

## Findings

- Actuator is in place (ADR-023): only `/actuator/health` is exposed,
  without details. It includes the database, so a database outage
  turns the container `unhealthy`. Separating liveness from readiness
  is pending before the Terraform service is defined.
- The `mvnw` wrapper exists but is not used by CI or by the Docker
  build (ADR-022 settles this debt). Unifying them remains a future
  decision.
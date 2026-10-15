# "It's green" criterion for Phase 3

Current version: 2026-10-11 (week 0). It is extended week by week,
as Docker, Actuator and Terraform come in. It starts from the 13-step
list in the Phase 3 plan.

## Current criterion

### Part A: build and tests (plan step 1)

From `codexrm-server`, with Docker Desktop open (Testcontainers
starts PostgreSQL):

    mvn clean verify

It is green if it ends in `BUILD SUCCESS`, with no failures, with at
least 142 unit tests and 36 integration tests (entry baseline for
Phase 3, measured with the surefire and failsafe reports).

### Part B: manual smoke test of the `.jar` (plan steps 3, 9 and 11)

1. Start the database (`docker compose up -d db`) and start the `.jar`
   with the `prod` profile and the variables from
   `docs/infra/environment-variables.md`.
2. `POST /api/auth/signin` with a test user: it must return
   `200` with a token. The rate limit in `prod` is
   5 attempts per minute per IP.
3. In the console, the logs must be JSON, and those of a
   request carry `correlationId`.

## Relationship with CI

CI runs `mvn clean test` and
`mvn failsafe:integration-test failsafe:verify`. These are the same
tests as Part A, but CI does not run the `package` phase: it does not
build or repackage the `.jar`. A packaging problem (such as the
`spring-boot-maven-plugin` without a version, fixed in the entry
gate) is not detected by CI, but is detected by `mvn clean verify`.

## Plan steps that do not apply yet

| Step | What it checks | Why it does not apply today |
|---|---|---|
| 2 | `docker build` | The production image is missing (weeks 1-2) |
| 4 | `GET /actuator/health` | Actuator is missing, see findings |
| 5-8, 12 | `terraform fmt`, `validate`, `plan`, `apply`, `destroy` | Terraform comes in during weeks 5-8 |
| 10 | The import goes to S3 | It is task 3.5 |
| 13 | Repeat against real AWS | Real AWS window (weeks 11-13) |

## Findings

- The `README.md` documents `/actuator/health`, but the `pom.xml` does
  not include `spring-boot-starter-actuator`: the endpoint does not
  exist today. The plan resolves it in weeks 1-2.
- The `mvnw` wrapper exists, but neither CI nor the project
  instructions use it: CI uses the runner's `mvn` and locally the
  installed `mvn` is used. It is left as is; unifying them for greater
  reproducibility remains a future decision.
# Phase 1 Retrospective

## What went well?

- The test safety net (134 unit + 21 integration tests) made it possible
  to find and fix real vulnerabilities with confidence before the phase
  closed: a hardcoded JWT secret and admin password in production
  (#136), and a path traversal in the export endpoint that allowed
  writing files outside the expected folder (#137) — the most serious
  finding of the whole phase.
- Error handling became unified: all failure paths (business
  exceptions, 401, 403, malformed JSON, files that are too large)
  return the same `ErrorResponse` structure, instead of mixing formats
  depending on which layer rejected the request.
- Every fix was validated with real evidence — startup logs,
  `mvn verify`, GitHub Actions screenshots — instead of assuming that
  "it should work".
- The discipline of one PR per issue, with commits explaining the
  *why* and not just the *what*, left a git history that can be
  audited week by week.

## What was harder than expected?

A version compatibility problem between Testcontainers and the local
Docker Engine, not in the project's code. When running the integration
tests (Spring Boot + JUnit 5 + Testcontainers + PostgreSQL from
IntelliJ on Windows), Testcontainers detected the Docker socket
(`npipe://`) but the daemon answered with a
`BadRequestException` (400) and empty fields (`ServerVersion`,
`Driver`, `OperatingSystem`, `KernelVersion`), always ending in
`Could not find a valid Docker environment`.

The root cause: Testcontainers 1.21.3 uses Docker API version 1.32 by
default, but the local Docker Engine (29.4.3) requires at least
1.40 — an API version incompatibility, not an installation or project
configuration problem. Everything else was ruled out along the way:
Maven dependencies, project reimport, and the `BaseIntegrationTest`
configuration, which had been correct from the start.

The solution was to update `testcontainers.version` from `1.21.3` to
`2.0.5` in the `pom.xml` (a simpler alternative than forcing
`api.version=1.41` through `systemPropertyVariables` in the Surefire
plugin). Resolving that incompatibility revealed a second, smaller
problem: `RefreshToken` used
`@GeneratedValue(strategy = GenerationType.AUTO)`, which Hibernate
against PostgreSQL in this context did not resolve consistently;
changing it to `GenerationType.IDENTITY` fixed it. With both changes,
`ApplicationIntegrationTest` (and the rest of the critical suite) went
back to running green and stable.

## What did I cut or postpone?

- **Package restructuring (week 19):** explicitly evaluated against the
  plan's 4 entry conditions. Three were met (critical suite green, CI
  green, no critical bugs open), but there was no concrete reason to
  move files now — the current structure caused no real friction
  during the whole phase. Documented in ADR-012.
- **Exhaustive OWASP audit of import/export:** the week 17 hardening
  closed the critical finding (path traversal), but a more exhaustive
  and systematic OWASP review is left for Phase 2, according to the
  cut plan itself.
- **`UserService` ↔ API DTO coupling:** during the week 19 evaluation
  it was found that `UserService` receives `api` layer types directly
  (unlike `ReferenceService` and `RoleService`, which do respect the
  layer boundary). Fixing it involves changing method signatures, so
  it was documented as explicit debt (ADR-012) instead of forcing it
  in a documentation-only week.

A similar but smaller finding — ownership logic living as a private
method in `ReferenceController` instead of in the service — was fixed
during this week's final verification, because it was low risk
(private method, no external contract) and did not require touching
any test beyond one.

## What did I learn that I did not expect?

Real security, beyond "use HTTPS" or "don't share passwords". Concrete
things such as: a JWT secret that is one character too short breaks
the whole app startup (and it has to be generated with a verifiable
command, not by copying and pasting by eye); an unsanitized file name
parameter can turn into an arbitrary file write; and that the
difference between "it works on my machine" and "it is verified" is
running the command and reading the result, not trusting the memory of
the previous run.

## Phase post-mortem

[JWT Secret Length Failures Across Environments](../post-mortems/fase-1-jwt-secret-length.md) —
a real bug, repeated three times during issue #136, with impact,
root cause, fix and prevention documented while the context was
fresh.

## Project status at phase close

- [x] Updated stack (Java 21, Spring Boot 3.2+)
- [x] `WebSecurityConfigurerAdapter` removed (migrated to `SecurityFilterChain`)
- [x] Docker working for local development
- [x] Spring Profiles configured (dev/test/prod, verified working)
- [x] Centralized CORS (`CorsConfig`/`CorsProperties`, no `@CrossOrigin` in controllers)
- [x] Flyway with versioned migrations (`app_user` table, not `"User"`)
- [x] `@Valid` in all controllers (13/13 `@RequestBody` confirmed)
- [x] There is no `return null` where `Page.empty()` should be (one case found in `buildPageDTO` turned out to be unreachable dead code, with no impact on the API)
- [x] Centralized error handling (`GlobalExceptionHandler`, `AuthEntryPointJwt`, `AccessDeniedHandlerImpl`, same `ErrorResponse` structure)
- [x] DTOConverter refactored
- [x] Role logic without duplication
- [x] Thin controllers (fixed: `filterUserReferences` moved from `ReferenceController` to `ReferenceService` as `filterOwnedReferences`)
- [x] Normalized REST URLs (24 routes audited: plural, lowercase, no verbs as resource name)
- [x] Unit tests and critical integration suite green (140 unit + 21 integration, `BUILD SUCCESS`)
- [x] Basic CI green (last 7+ runs on `dev` green in GitHub Actions)
- [x] Minimum useful documentation complete (README, `architecture.md`, `decision-log.md`, `testing.md`, `integration-testing.md`)
- [x] A phase retrospective and a short post-mortem exist
- [x] Package restructuring executed or postponed with explicit criteria (postponed, ADR-012)

## Am I ready for Phase 2?

**Yes.** The 18 items of the closure checklist are confirmed with real
evidence (not from memory): `mvn verify` output, GitHub Actions
screenshots, and direct searches in the code during this same week.
The two architecture findings that appeared along the way
(`UserService` coupling with API DTOs, and the restructuring gate)
were documented with explicit criteria instead of being ignored or
forced out of time — which is exactly the behavior Phase 1 aimed to
install as a habit.

The pending debt (ADR-012, exhaustive OWASP audit) is known, written
down, and blocks nothing in the current functioning of the system.

## Phase 2 Preview — What's coming

- Deep review of authorization rules in `SecurityFilterChain`
- `SecurityScheme` for JWT in Swagger UI ("Authorize" button)
- Stricter input validation (OWASP)
- Residual OWASP audit on import/export and other endpoints
- Resolve the UserService ↔ API DTO coupling (ADR-012)
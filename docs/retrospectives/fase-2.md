# Phase 2 Retrospective

## What went well?

The discovery of the double login bug (weeks 4-6) was the best
achievement of the whole phase: it did not come from a report by an
affected user, but from a deliberate audit of the JWT/refresh token
lifecycle — reading the schema, noticing the `UNIQUE` constraint on
`user_id`, formulating the hypothesis "this should break on a second
login", and confirming it live before it ever became a real problem.
It is exactly the kind of discipline this phase aimed to install: not
waiting for something to break in order to investigate it.

Also going well: the `correlationId` working end to end from the first
live attempt (the same UUID in the response header and in the
`AuthEntryPointJwt` log, a path that runs outside the normal
controller flow); and the fact that 3 of the phase's 4 real security
findings (double login, plaintext JWT, AUDITOR/logout) came out of
deliberate audits, not by chance.

## What was harder than expected?

Switching between Docker (`prod` profile) and `mvn spring-boot:run`
(`dev` profile) for one-off verifications caused recurring friction
throughout the phase: different datasource credentials
(`postgres`/`postgres` vs `codexrm`/`codexrm`), port conflicts when
both were left running at once, and the need to remember to revert
temporary changes in `application-dev.properties` before each commit.
None of these was a project bug — all were local environment
friction — but they consumed real time in more than one verification
(Swagger with JWT, double login, rate limiting).

## What did I cut or postpone?

- **ADR-012 (`UserService`/api DTO coupling):** explicitly evaluated in
  week 0 against the 5 categories of the Phase 2 gate (auth, OWASP,
  observability, rate limiting, runbooks) — it fits none. It is
  general architecture debt, not security, and is postponed to
  Phase 3.
- Nothing else was left postponed and unresolved within the real scope
  of Phase 2 — the 4 security findings found during the phase were
  fixed on the spot, not left for later.

## What did I learn that I did not expect?

That a well-designed generic error fallback (the standard `500` from
`GlobalExceptionHandler`, without leaking internal details) can
perfectly hide a completely preventable bug for a long time — the
double login was there, undetected, protected by error handling that
did its job correctly but did not warn that the case itself was
avoidable. I also learned how fragile a seemingly innocent combination
like `@Component` + `@MockBean` can be in Spring Boot: adding a
standard annotation silently broke every controller test in the
project, with no error message pointing to the real cause.

## Phase post-mortem

[Double Login Crashes with 500](../post-mortems/fase-2-double-login-crash.md) —
impact, root cause, fix and prevention documented for the most
valuable finding of the phase.

## Closure checklist (verified with evidence, week 12)

- [x] Authorization hardened (matrix current, confirmed via `git log`)
- [x] JWT/refresh tested (`mvn verify`, upsert + rotation)
- [x] CORS per environment (different dev/prod, no wildcard)
- [x] OWASP import/export closed (file names 100% server-generated, confirmed via `git log`)
- [x] Structured logs visible (JSON, correlationId present)
- [x] Rate limiting with 429 tested (verified live and in tests)
- [x] Expanded critical suite green (`mvn verify`, 145+ unit / 35+ integration)
- [x] Load baseline documented (k6, 2 scenarios)
- [x] Runbooks published (all 3, verified live)
- [x] No critical vulnerability without a fix (4 findings, all resolved)

See [ADR-019](../decision-log.md) for the full detail of the closure.

## Am I ready for Phase 3?

**Yes.** The 10 items of the closure checklist are confirmed with real
evidence generated today (`mvn verify` output, `git log` history on
the critical files, not memory of earlier weeks). The 4 real security
findings of the phase were found through deliberate audit and fixed on
the spot, none left pending. The only known debt (ADR-012) is general
architecture, explicitly outside the security scope of Phase 2,
documented as a Phase 3 candidate instead of ignored.

## Phase 3 Preview

Without a detailed plan yet — to be defined. Known candidates from
this phase: resolve ADR-012 (`UserService` coupling), and any topic
that arises from operating the system with observability and rate
limiting already in production.
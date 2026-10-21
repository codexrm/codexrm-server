# Documentation Index

Documentation of `codexrm-server`. All documents are written in English
([ADR-021](decision-log.md)).

## Architecture and decisions

| Document | What it covers |
|---|---|
| [architecture.md](architecture.md) | Overview of the server architecture |
| [decision-log.md](decision-log.md) | Architecture decision records (ADR-001 to ADR-021) |
| [refactor-plan.md](refactor-plan.md) | Phase 1 refactoring plan and its outcome |

## Testing and CI

| Document | What it covers |
|---|---|
| [testing.md](testing.md) | Test layers, what to run and when, and the Phase 2 validation baseline |
| [integration-testing.md](integration-testing.md) | The critical integration suite and what it protects |
| [ci-validation.md](ci-validation.md) | What the CI workflow does and how to validate locally |
| [ci-strategy.md](ci-strategy.md) | How CI resolves the internal Maven dependencies (GitHub Packages) |

## Security

| Document | What it covers |
|---|---|
| [security/authentication-flow.md](security/authentication-flow.md) | Authentication and authorization flow traced from the real code |
| [security/authorization-matrix.md](security/authorization-matrix.md) | Permissions by role and ownership policy per endpoint |
| [security/endpoint-exposure-inventory.md](security/endpoint-exposure-inventory.md) | Baseline inventory of every endpoint before the Phase 2 hardening |
| [security/jwt-refresh-token-audit.md](security/jwt-refresh-token-audit.md) | Audit of the JWT/refresh token lifecycle (Phase 2) and its gaps |

## Observability and performance

| Document | What it covers |
|---|---|
| [observability/event-catalog.md](observability/event-catalog.md) | Business events, log fields and logging rules |
| [performance/load-baseline.md](performance/load-baseline.md) | Load test baseline (k6, 2 scenarios) |

## Runbooks

| Document | What it covers |
|---|---|
| [runbooks/auth-401-403.md](runbooks/auth-401-403.md) | Diagnosing 401 and 403 responses |
| [runbooks/refresh-token-failed.md](runbooks/refresh-token-failed.md) | Diagnosing refresh token failures |
| [runbooks/import-export-rejected.md](runbooks/import-export-rejected.md) | Diagnosing rejected imports and exports |

## Infrastructure (Phase 3)

| Document | What it covers |
|---|---|
| [infra/environment-variables.md](infra/environment-variables.md) | Required environment variables for the `prod` profile |
| [infra/green-criterion.md](infra/green-criterion.md) | The "green" criterion for Phase 3 (build, tests, smoke test) |
| [infra/local-filesystem-inventory.md](infra/local-filesystem-inventory.md) | Inventory of local disk usage (input for task 3.5) |

## Post-mortems and retrospectives

| Document | What it covers |
|---|---|
| [post-mortems/fase-1-jwt-secret-length.md](post-mortems/fase-1-jwt-secret-length.md) | JWT secret length failures across environments (Phase 1) |
| [post-mortems/fase-2-double-login-crash.md](post-mortems/fase-2-double-login-crash.md) | Double login crash with a 500 (Phase 2) |
| [retrospectives/fase-1.md](retrospectives/fase-1.md) | Phase 1 retrospective |
| [retrospectives/fase-2.md](retrospectives/fase-2.md) | Phase 2 retrospective |

## Code reading notes

| Document | What it covers |
|---|---|
| [code-reading/modelmapper.md](code-reading/modelmapper.md) | Inheritance and polymorphic mapping with ModelMapper (ADR-007) |
| [code-reading/springdoc-openapi.md](code-reading/springdoc-openapi.md) | springdoc-openapi configuration and DTO inheritance (ADR-008) |
| [code-reading/spring-data-jpa.md](code-reading/spring-data-jpa.md) | Spring Data JPA repositories and query derivation |

## Conventions

- Documents are written in English; new ones follow ADR-021.
- Some file names keep the original `fase-N` spelling (post-mortems and retrospectives); renaming them would break existing links.
- Architecture decisions are recorded in `decision-log.md` as ADRs (Status, Context, Decision, Consequences).
- When a document is added or removed, update this index.
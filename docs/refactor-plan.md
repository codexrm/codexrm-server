# Refactor Plan (Phase 1, Weeks 9–16)

## Status

Historical planning document for Phase 1. Outcome, according to the Phase 1 retrospective (`docs/retrospectives/fase-1.md`) and ADR-012:

| PR | Item | Outcome |
|---|---|---|
| 1 | GlobalExceptionHandler | Done |
| 2 | RoleService (extract role logic) | Done |
| 3 | DTOConverter (registry pattern) | Done |
| 4 | Thin controllers | Done |
| 5 | Normalize REST URLs | Done |
| 6 | Update OpenAPI | Done (#84) |
| 7 | Restructure packages | Postponed with explicit criteria (ADR-012) |

The week numbers below are those of the original plan. The package restructuring was evaluated in week 19, not in weeks 15–16.

## Objective

Refactor the system safely using the unit tests created as a safety net.

---

## Refactoring Order

1. Validation layer
2. Services
3. Controllers
4. General cleanup

---

## Details

### 1. Validation

- Improve validation structure
- Reduce duplicated validation logic
- Ensure consistency across all reference types

---

### 2. Services

- Simplify business logic
- Remove redundant code
- Improve exception handling

---

### 3. Controllers

- Ensure consistent responses
- Improve request validation (`@Valid`)
- Standardize error handling

---

### 4. Cleanup

- Remove unused code
- Improve naming
- Organize packages if needed

---

## Risks

- Breaking existing behavior
- Introducing bugs during refactoring

---

## Mitigation

- Use unit tests as safety net
- Refactor in small steps
- Test after each change

---

## Notes

- No new features will be added during refactoring
- Focus is on code quality and maintainability

---

## PR Dependency Graph (Weeks 9–13)

### Weeks 9–11: Error Handling and Cleanup

PR 1: GlobalExceptionHandler ────────────────┐
│
PR 2: RoleService (extract role logic) ─────┤
├── PR 4: Thin Controllers
PR 3: DTOConverter (registry pattern) ──────┘    (depends on 1, 2 and 3)

---

### Weeks 12–13: REST Normalization

PR 5: Normalize REST URLs ──── PR 6: Update OpenAPI
(first)                       (second)

---

### Weeks 15–16: Architecture

PR 7: Restructure packages (final step, after integration tests are stable)

---

## Notes

- Package restructuring is intentionally delayed to avoid breaking imports and tests
- Each PR should be small and independently testable
- Order must be respected to avoid conflicts
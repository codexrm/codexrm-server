# Endpoint Exposure Inventory

Baseline inventory of every API endpoint before hardening (Fase 2,
Semanas 1-3). Confirmed against `AuthController`,
`ReferenceController`, `UserController` and `WebSecurityConfig`.

## Public endpoints (by design)

| Method | Path | Notes |
|---|---|---|
| POST | `/api/auth/signup` | Covered by `/api/auth/**` in `permitAll()` |
| POST | `/api/auth/signin` | Same |
| POST | `/api/auth/refresh-token` | Same |
| POST | `/api/auth/logout` | Same — flagged for review: should logout require an authenticated JWT instead of being fully public? |
| — | `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**` | Public in all profiles today — prod exposure decision pending (Issue 2) |
| — | `/error` | Standard Spring error fallback |

## Protected endpoints — References (`/api/references`)

| Method | Path | @PreAuthorize | Notes |
|---|---|---|---|
| GET | `/api/references` | `hasRole('USER')` | |
| GET | `/api/references/{id}` | `hasRole('USER')` | Ownership enforced in service |
| POST | `/api/references` | `hasRole('USER')` | |
| PUT | `/api/references/{id}` | `hasRole('USER')` | |
| DELETE | `/api/references/{id}` | `hasRole('USER')` | |
| DELETE | `/api/references` | `hasRole('USER')` | Bulk delete, ownership filtered |
| POST | `/api/references/sync` | `hasRole('USER')` | |
| POST | `/api/references/import` | `hasRole('USER')` | |
| POST | `/api/references/export` | `hasRole('USER')` | Ownership filtered |
| GET | `/api/references/all-users` | `hasRole('MANAGER')` | ⚠️ **Flagged**: no `ADMIN` access — confirm if intentional |

## Protected endpoints — Users (`/api/users`)

| Method | Path | @PreAuthorize | Notes |
|---|---|---|---|
| GET | `/api/users` | `hasRole('ADMIN')` | |
| GET | `/api/users/{id}` | `hasRole('ADMIN') or hasRole('USER') or hasRole('AUDITOR')` | ⚠️ **Flagged**: internal ownership check (`userDetails.getId().equals(id) \|\| isAdmin()`) means `AUDITOR` can never actually pass — confirm if intentional |
| POST | `/api/users` | `hasRole('ADMIN')` | |
| PUT | `/api/users/{id}` | `hasRole('ADMIN')` | |
| PUT | `/api/users/password` | `hasRole('USER')` | |
| PUT | `/api/users/preferences` | `hasRole('USER')` | |
| DELETE | `/api/users/{id}` | `hasRole('ADMIN')` | |
| DELETE | `/api/users` | `hasRole('ADMIN')` | |
| GET | `/api/users/me` | `hasRole('USER') or hasRole('ADMIN')` | |

## Flagged for Issue 2 (hardening) decision

1. `GET /api/references/all-users` — should `ADMIN` also have access,
   or is `MANAGER`-only intentional?
2. `GET /api/users/{id}` — `AUDITOR` role is granted by
   `@PreAuthorize` but can never pass the internal ownership check.
   Either the role is dead weight here, or the intent was for
   `AUDITOR` to see any user (bypass ownership) — needs a decision.
3. `POST /api/auth/logout` — fully public (no JWT required). Confirm
   this is intentional (e.g., idempotent/no-op if no session) rather
   than an oversight.
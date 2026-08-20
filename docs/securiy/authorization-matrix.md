# Authorization Matrix

Consolidated from the endpoint exposure inventory (#149), the
hardening decisions (#150), and the ownership policy (#151, ADR-014).
Verified directly against `@PreAuthorize` annotations in
`AuthController`, `ReferenceController`, and `UserController`, and
against `WebSecurityConfig`'s `authorizeHttpRequests`.

## Roles

| Role | Description |
|---|---|
| `ROLE_USER` | Standard authenticated user, owns their own references |
| `ROLE_MANAGER` | Can view references across all users (read-only, cross-user) |
| `ROLE_ADMIN` | Full user and role management, full reference visibility |
| `ROLE_AUDITOR` | Read-only visibility into any user's profile |

## Public endpoints (no role required)

| Method | Path | Notes |
|---|---|---|
| POST | `/api/auth/signup` | |
| POST | `/api/auth/signin` | |
| POST | `/api/auth/refresh-token` | |
| GET | `/swagger-ui.html`, `/v3/api-docs/**` | Public only in `dev`/`test` — requires JWT in `prod` (ADR-013) |

## Authenticated, no specific role (any logged-in user)

| Method | Path | Notes |
|---|---|---|
| POST | `/api/auth/logout` | Requires valid JWT (ADR: hardened in #150) |

## References (`/api/references`)

| Method | Path | Required Role | Ownership Enforced | On Non-Owner | On Non-Existent |
|---|---|---|---|---|---|
| GET | `/api/references` | `USER` | Yes (scoped to own) | — | — |
| GET | `/api/references/{id}` | `USER` | Yes | 403 | 404 |
| POST | `/api/references` | `USER` | — (creates own) | — | — |
| PUT | `/api/references/{id}` | `USER` | Yes | 403 | 404 |
| DELETE | `/api/references/{id}` | `USER` | Yes | 403 | 404 |
| DELETE | `/api/references` (bulk) | `USER` | Yes (silently filtered) | non-owned IDs dropped | — |
| POST | `/api/references/sync` | `USER` | Yes | 403 | 404 |
| POST | `/api/references/import` | `USER` | — (creates own) | — | — |
| POST | `/api/references/export` | `USER` | Yes (silently filtered) | non-owned IDs dropped | — |
| GET | `/api/references/all-users` | `MANAGER` only | N/A (cross-user by design) | — | — |

## Users (`/api/users`)

| Method | Path | Required Role | Ownership Enforced | On Non-Owner | On Non-Existent |
|---|---|---|---|---|---|
| GET | `/api/users` | `ADMIN` | N/A (full listing by design) | — | — |
| GET | `/api/users/{id}` | `ADMIN`, `USER`, `AUDITOR` | Yes for `USER` only (`ADMIN`/`AUDITOR` bypass) | 403 (USER only) | 404 |
| POST | `/api/users` | `ADMIN` | — | — | — |
| PUT | `/api/users/{id}` | `ADMIN` | — | — | — |
| PUT | `/api/users/password` | `USER` | Implicit (self only) | — | — |
| PUT | `/api/users/preferences` | `USER` | Implicit (self only) | — | — |
| DELETE | `/api/users/{id}` | `ADMIN` | — | — | — |
| DELETE | `/api/users` (bulk) | `ADMIN` | — | — | — |
| GET | `/api/users/me` | `USER`, `ADMIN` | Implicit (self only) | — | — |

## Ownership response policy (ADR-014)

- Resource doesn't exist → `404 Not Found`.
- Resource exists but belongs to another user → `403 Forbidden`.
- Applied identically across references and users.
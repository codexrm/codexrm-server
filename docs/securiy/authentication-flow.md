# Authentication Flow — Traced from Real Code

This traces the actual authentication/authorization flow of
`codexrm-server`, reading the real classes and methods — not a
generic Spring Security diagram. Use this as the starting point when
diagnosing an auth-related incident (see
`docs/runbooks/auth-401-403.md`).

## Filter chain order

Registered in `WebSecurityConfig.filterChain()`, in this exact order:
1. CorrelationIdFilter (registered separately, highest precedence —
runs before the entire security chain)
2. AuthRateLimitFilter (http.addFilterBefore, before UsernamePasswordAuthenticationFilter)
3. AuthTokenFilter (http.addFilterBefore, same position)
4. authorizeHttpRequests rules (URL-level, checked by AuthorizationFilter)
5. @PreAuthorize (method-level, checked when entering the controller method)
6. Controller method body


## Step by step

### 1. `CorrelationIdFilter`
Registered via a dedicated `FilterRegistrationBean` in `LoggingConfig`
with `Ordered.HIGHEST_PRECEDENCE` — runs before anything else,
including the whole Spring Security chain. Generates (or reuses, via
`X-Correlation-Id` header) a UUID, puts it in MDC, echoes it in the
response header. Every log line from this point on, in every
component below, includes this ID. Cleared in `finally`, always.

### 2. `AuthRateLimitFilter`
Only acts on `POST /api/auth/signin` and `POST /api/auth/refresh-token`
(`appliesTo()`). Keyed by `request.getRemoteAddr()` (client IP), using
an in-memory Bucket4j bucket per IP. Threshold configurable per
profile (`codexrm.ratelimit.auth.*`; 5/min in prod). If exceeded,
responds directly with `429` (standard `ErrorResponse` shape) and
**does not call `filterChain.doFilter(...)`** — the request never
reaches any later filter, including JWT validation.

### 3. `AuthTokenFilter`
Extracts the JWT from the `Authorization: Bearer <token>` header
(`parseJwt`). If present and valid (`jwtUtils.validateJwtToken`),
loads the user via `UserDetailsServiceImpl.loadUserByUsername` and
populates the `SecurityContextHolder` with a
`UsernamePasswordAuthenticationToken`. If missing or invalid, the
`SecurityContext` stays empty and the request continues — the
rejection (if any) happens later, at step 4 or 5.

### 4. `authorizeHttpRequests` (URL-level)
Public without authentication: `/api/auth/signup`, `/api/auth/signin`,
`/api/auth/refresh-token`, `/error`, and — **only outside `prod`** —
`/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs/**` (checked via
`environment.getActiveProfiles()`). Everything else requires
authentication (`anyRequest().authenticated()`). If the
`SecurityContext` is empty here (no valid JWT from step 3), Spring
Security's `ExceptionTranslationFilter` invokes
`unauthorizedHandler` → **`AuthEntryPointJwt`** → **401**, using the
standard `ErrorResponse` shape.

### 5. `@PreAuthorize` (method-level)
Each controller method declares its own role requirement (e.g.
`hasRole('USER')`, `hasRole('ADMIN')`, or combinations — see
`docs/security/authorization-matrix.md` for the full table). If the
authenticated user's role doesn't satisfy it, Spring Security's
method-security interceptor throws `AccessDeniedException`, caught by
**`AccessDeniedHandlerImpl`** (registered via
`.exceptionHandling().accessDeniedHandler(...)`) → **403**.

### 6. Inside the controller/service (ownership)
Some endpoints (e.g. `GET /api/users/{id}`, references CRUD) pass
`@PreAuthorize` for the role but still enforce per-resource ownership
in code (`UserController.isAdmin`/`isAuditor` checks,
`ReferenceService.validateOwnership`). A failed ownership check
throws `InvalidOperationException`, caught by
**`GlobalExceptionHandler`** → **403** (per ADR-014's ownership
policy: 403 for "exists but not yours", 404 via
`ResourceNotFoundException` for "doesn't exist at all").

## Which component produces which status

| Status | Component | Trigger |
|---|---|---|
| 401 | `AuthEntryPointJwt` | Missing or invalid JWT on a protected route |
| 403 (filter-level) | `AccessDeniedHandlerImpl` | Authenticated, but `@PreAuthorize` role check fails |
| 403 (ownership) | `GlobalExceptionHandler` (`InvalidOperationException`) | Authenticated, correct role, but not the resource owner |
| 404 | `GlobalExceptionHandler` (`ResourceNotFoundException`) | Resource genuinely doesn't exist |
| 429 | `AuthRateLimitFilter` | Too many `/signin` or `/refresh-token` attempts from the same IP |

All five use the same `ErrorResponse` structure (ADR-011):
`timestamp`, `status`, `error`, `message`, `path`.

## Login → refresh → logout lifecycle

See `docs/security/jwt-refresh-token-audit.md` and ADR-015 for the
full policy. Summary:
- **Login** (`POST /api/auth/signin`): `createRefreshToken()` upserts
  — reuses the existing row for the user if one exists (fixed in
  weeks 4-6 after finding a double-login 500 bug), instead of
  inserting a duplicate against the `user_id UNIQUE` constraint.
- **Refresh** (`POST /api/auth/refresh-token`): `rotateRefreshToken()`
  invalidates the token used and issues a new one. The old token
  cannot be reused afterward.
- **Logout** (`POST /api/auth/logout`, requires a valid JWT since
  weeks 1-3): `deleteByUserId()` removes the user's refresh token.

## Observability

Every request's `correlationId` (from step 1) appears in every log
line for that request, including the ones generated by
`AuthEntryPointJwt`, `AccessDeniedHandlerImpl`, and
`AuthRateLimitFilter` — none of which run inside the normal
controller flow. See `docs/observability/event-catalog.md` for the
full business event catalog.
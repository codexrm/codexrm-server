## JWT / Refresh Token Lifecycle Audit

> **Status:** This document records the audit as it was at the time (Phase 2, weeks 4-6). Gaps 1 and 2 were fixed in the same phase: `createRefreshToken()` now upserts and every refresh rotates the token (ADR-015). Gap 3 (expired token mapped to 404) is not addressed in ADR-015.

### Expiration
- JWT: `codexrm.app.jwtExpirationMs` — 3,600,000 ms (1 hour), the same
  in dev/test/prod.
- Refresh token: `codexrm.app.jwtRefreshExpirationMs` — 864,000,000 ms
  (10 days), the same in dev/test/prod.

### Creation
- A refresh token is created on every `POST /api/auth/signin`, through
  `RefreshTokenService.createRefreshToken(userId)`.
- The `refreshtoken` table has `user_id UNIQUE` — an intentional design
  of 1 active token per user (`OneToOne`, according to the comment in
  the migration).

### 🔴 CRITICAL BUG FOUND: double login crashes with 500
`createRefreshToken()` always inserts a new `RefreshToken` without
checking whether the user already has one. Since `user_id` is `UNIQUE`,
a second `signin` by the same user without a prior logout (login from
another device/tab, or a retry) breaks with:

ConstraintViolationException: duplicate key value violates unique
constraint "refreshtoken_user_id_key"


Confirmed live via Swagger UI — a real 500 reproduced with 2
consecutive signins of the same user. There is no rotation or
replacement of the existing token; the second attempt simply fails.

### Invalidation on logout
- `POST /api/auth/logout` (already requires a JWT since #150) calls
  `refreshTokenService.deleteByUserId(userId)`, which deletes the
  user's refresh token(s). It works correctly when used.

### Expiration check
- `verifyExpiration()` checks `expiryDate` against `Instant.now()` and
  deletes the token if it has expired, throwing `TokenRefreshException`
  (maps to 404 in `GlobalExceptionHandler`, per ADR-014's exception
  mapping — although semantically an expired token is more a 401 than
  a 404; worth reviewing whether to adjust it, since it is not
  "not found" but "found but invalid").

### No rotation on refresh
- `POST /api/auth/refresh-token` generates a new JWT, but **reuses the
  same refresh token** indefinitely until it expires — there is no
  rotation (invalidating the old one and issuing a new one on each
  use). This is a weak security practice: if a refresh token leaks, it
  stays valid until its natural expiration (10 days), with no signal
  of misuse.

## Summary of gaps to fix in Issue 2
1. **Critical**: double login must not crash with 500 — needs an
   upsert/replace strategy in `createRefreshToken()`.
2. **Medium**: no refresh token rotation on use — a filtered token
   stays valid up to 10 days with no detection mechanism.
3. **Minor, needs decision**: expired refresh token maps to 404
   (`ResourceNotFoundException`-style) via `TokenRefreshException`,
   when 401 might be more semantically correct — flagging for
   discussion, not necessarily a required fix.
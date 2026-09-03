# Post-Mortem: Double Login Crashes with 500 (Refresh Token Unique Constraint)

**Phase:** Fase 2, weeks 4-6 (JWT/CORS/OWASP hardening)
**Severity:** Critical — a completely normal, common user action
crashed the application, in production behavior terms (reproduced
against the `dev`-profile app during the audit).

## Impact

Any user logging in a second time — from a second browser tab, a
second device, or simply retrying after a page refresh — without
having logged out first, received an unhandled `500 Internal Server
Error` on `POST /api/auth/signin`. Their first session remained
unaffected, but the second, entirely legitimate login attempt
crashed instead of succeeding.

This is not an edge case or an attack scenario — it's one of the
most common things a real user does. The bug had likely been present
since the refresh token feature was first built, undetected because
manual testing rarely exercises "log in twice without logging out
first" as a deliberate scenario.

## Root Cause

`RefreshTokenService.createRefreshToken(userId)` always constructed
and saved a **new** `RefreshToken` entity on every call, with no
check for whether the user already had one:

```java
// Before the fix
RefreshToken refreshToken = new RefreshToken();
refreshToken.setUser(user);
refreshToken.setExpiryDate(...);
refreshToken.setToken(UUID.randomUUID().toString());
return refreshTokenRepository.save(refreshToken);
```

The `refreshtoken` table's schema (`V1__initial_schema.sql`) has a
`UNIQUE` constraint on `user_id` — a deliberate one-token-per-user
design (documented as `OneToOne` in the migration's own comment).
On a second `signin` for the same user, this `save()` call attempted
a second `INSERT` for the same `user_id`, violating the constraint.
The resulting `ConstraintViolationException` propagated up
unhandled to `GlobalExceptionHandler`'s generic fallback, producing
a `500` with the message `"An unexpected error occurred"` — correct
in that it didn't leak internals to the client (per ADR-011), but
wrong in that a foreseeable, common scenario was being treated as an
unexpected server fault.

## How It Was Found

Discovered during a deliberate JWT/refresh-token lifecycle audit
(week 4-6, issue #155) — not by a user report. While tracing the
`refreshtoken` table's schema and confirming the `user_id UNIQUE`
constraint's implication, the hypothesis "a second login should
violate this constraint" was formed and tested directly: two
consecutive `POST /api/auth/signin` calls for the same user via
Swagger UI. The first succeeded (200); the second failed with the
predicted 500, with the exact constraint-violation message visible
in the server logs.

## Fix

`createRefreshToken()` was changed to an upsert: look up an existing
`RefreshToken` for the user first, and reuse that row (assigning a
fresh token value and expiry) instead of always constructing a new
one:

```java
RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
        .orElseGet(RefreshToken::new);

refreshToken.setUser(user);
refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
refreshToken.setToken(UUID.randomUUID().toString());

return refreshTokenRepository.save(refreshToken);
```

A new repository method, `findByUser(User user)`, was added to
support the lookup. As a related hardening (not required to fix this
specific bug, but a natural improvement while touching this code),
refresh token **rotation** was also added: each use of
`/refresh-token` now invalidates the token used and issues a new
one, rather than reusing the same token value for its full 10-day
lifetime.

Verified live: reproduced the 500 before the fix, then confirmed 200
with a fresh (different) refresh token on the second login after the
fix, via the same manual Swagger UI reproduction used to find it.
Locked in with 3 new `AuthIntegrationTest` cases against a real
database.

## Prevention

- **Test deliberately for "do it twice" scenarios**, not just the
  happy path once. Any feature with a uniqueness constraint at the
  database level deserves an explicit test for what happens on a
  second legitimate attempt, not just a first one.
- When adding a `UNIQUE` constraint to a schema, immediately audit
  every code path that writes to that table for whether it assumes
  "always insert" or correctly handles "might already exist."
- The generic 500 fallback (ADR-011) did its job correctly — it
  didn't leak the SQL constraint name or stack trace to the client.
  But a clean, safe-looking 500 can still hide a completely
  preventable, common-case bug. A generic error handler is not a
  substitute for handling foreseeable cases explicitly; it's a
  safety net for the genuinely unexpected ones.
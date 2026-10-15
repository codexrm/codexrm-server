# Runbook — Refresh Token Failed

## Symptom

- `POST /api/auth/refresh-token` returns an unexpected `401`, `403` or `400`.
- The client reports that the refresh token "stopped working".
- Logout seems not to invalidate the refresh token, or invalidates it too early.

## How to reproduce

1. Log in and save the JWT + refresh token.
2. Call `POST /api/auth/refresh-token` with a valid token.
3. Repeat with an expired, malformed or revoked token.
4. Repeat after `POST /api/auth/logout`.

## What to check first

- Does the token exist in the database?
- Is it expired according to the current policy?
- Was it revoked by logout or by rotation (each use of
  `/refresh-token` invalidates the previous token and issues a new one)?
- Does the expiration configuration match the current profile?

## Expected logs and events

- Successful or failed refresh event with `correlationId`.
- General reason: expired, missing, revoked or invalid.
- Response consistent with the auth error contract
  (`ErrorResponse`, see `docs/decision-log.md` ADR-011).

## Likely causes

- Expiration misconfigured per environment.
- Refresh token revoked by logout, or invalidated by rotation after
  a previous use (expected behavior since week 4-6 of Phase 2).
- Divergence between the documented policy and the real implementation.
- Error in logout, rotation or token persistence.

## How to resolve

1. Confirm the real state of the token in the database (table
   `refreshtoken`, the `user_id` column is unique — only 1 active token
   per user).
2. Verify expiration and timezone with concrete data.
3. Review the documented policy in `docs/decision-log.md` and in
   `docs/security/jwt-refresh-token-audit.md`.
4. Reproduce with an integration test before fixing
   (`AuthIntegrationTest`).
5. If the bug affects logout or rotation, validate both flows before
   closing.

## How to prevent recurrence

- Keep tests for valid token, expired, revoked, effective logout,
  double login, and rotation (`AuthIntegrationTest`).
- Do not change expirations without updating configuration and docs.
- Record the lifecycle policy in `docs/decision-log.md`.

## Links

- `AuthIntegrationTest` (JWT/refresh lifecycle tests)
- `docs/security/jwt-refresh-token-audit.md`
- Expiration configuration per environment (`application-*.properties`)
- Refresh token policy ADR in `docs/decision-log.md`
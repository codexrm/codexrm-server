# Post-Mortem: JWT Secret Length Failures Across Environments

**Phase:** Fase 1, weeks 14-17 (security hardening)
**Severity:** Medium (blocked local startup and integration tests
multiple times; never reached production in a broken state)

## Impact

While externalizing the JWT secret from hardcoded values to
environment variables (issue #136), the application failed to start
on three separate occasions with:

```
IllegalArgumentException: JWT secret must be at least 64 characters
long for HS512
```

This happened in three different contexts over the course of the
same work session:

1. Docker Desktop startup (`application-prod.properties`, secret
   pulled from `.env`) — the value in `.env` was accidentally
   truncated to 16 characters when copy-pasting from a generator's
   output.
2. `mvn verify` failing all 21 integration tests — the value manually
   cleaned up in `application-integration.properties` (removing a
   duplicated-property-name bug, a separate issue) ended up 63
   characters instead of 64, one character short.
3. An earlier Docker attempt where the `docker-compose.yml`-embedded
   secret (before it was moved to `.env`) was also one character
   short of 64.

Each time, the fix required stopping, generating a new secret with
a verified length, and restarting — a few minutes of lost time per
occurrence, compounded across dev, test/integration, and prod-profile
configuration files.

## Root Cause

Two compounding causes:

1. **No tooling support for generating a correctly-sized secret.**
   The secret was typed/copy-pasted manually across four different
   properties files (`application-dev`, `application-test`,
   `application-integration`, and the `.env` used by
   `application-prod`), with no single source of truth and no
   automated check before restart/test-run that the value actually
   met the 64-character HS512 minimum.
2. **The validation only fires at Spring context startup**
   (`JwtUtils`'s `@PostConstruct` hook), which is correct for
   fail-fast behavior, but means a bad secret is only discovered
   after a full `mvn verify` run (up to ~1 minute) or a full Docker
   rebuild, rather than immediately when the value is entered.

## Fix

- Each affected properties file was corrected with a verified
  64-character value, checked explicitly before use with:
```bash
  echo -n "value" | wc -c
```
- The manual cleanup of a separate formatting bug (property name
  duplicated inside its own value, e.g.
  `codexrm.app.jwtSecret=codexrm.app.jwtSecret=...`) was redone
  carefully to avoid accidentally shortening the actual secret value
  again.

## Prevention

- `JwtUtils` already fails fast with a clear, specific error message
  (`"JWT secret must be at least 64 characters long for HS512"`)
  rather than allowing a weak secret to run silently — this is
  working as intended and should be kept.
- When generating or editing a JWT secret going forward, always
  verify its length with `echo -n "$SECRET" | wc -c` (or
  `openssl rand -hex 32`, which deterministically produces exactly
  64 hex characters) **before** pasting it into any properties file,
  rather than trusting a copy-paste to have preserved the full value.
- Prefer generating the secret once and reusing the same verified
  value across `dev`/`test`/`integration` (all non-sensitive,
  non-production profiles) instead of retyping or re-copying it into
  each file separately, which is where the truncation happened
  repeatedly.
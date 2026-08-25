# Business Event Catalog

Minimal catalog of deliberate business events logged by the
application — not framework noise, but events worth searching for
when diagnosing a real incident. Each event is structured JSON (see
`logback-spring.xml`) and includes the request's `correlationId`
automatically via MDC.

## Format

Every event follows the pattern `event=<name> key=value key=value...`
inside the log `message` field, so it's greppable even before JSON
parsing.

## Catalog

| Event | Level | Where | Fields | Meaning |
|---|---|---|---|---|
| `auth.login.success` | INFO | `AuthController.authenticateUser` | `username` | A user successfully authenticated |
| `auth.login.failed` | WARN | `AuthController.authenticateUser` | `username` | Authentication attempt failed (bad credentials) |
| `auth.refresh.success` | INFO | `AuthController.refreshtoken` | `username` | A refresh token was used successfully (and rotated) |
| `auth.refresh.failed` | WARN | `AuthController.refreshtoken` | `reason` | A refresh attempt failed (token not found; expiration is a separate path via `TokenRefreshException`) |
| `auth.logout` | INFO | `AuthController.logoutUser` | `username` | A user logged out (refresh token deleted) |
| `authorization.denied` | WARN | `AccessDeniedHandlerImpl` | `path`, `reason` | A request was rejected with 403 |
| `reference.sync` | INFO | `ReferenceController.sync` | `username`, `newCount`, `updatedCount`, `deletedCount` | A sync operation completed |
| `reference.import` | INFO/WARN | `ReferenceController.importReferences` | `username`, `format`, `outcome`, `count` or `reason` | An import succeeded, or was rejected (empty file / unsupported format) |
| `reference.export` | INFO | `ReferenceController.exportReferences` | `username`, `format`, `count` | An export completed |

## What is NEVER logged

- **Passwords** — never logged in any form, at any level, anywhere.
- **Full JWT tokens** — `AuthTokenFilter` previously logged the
  complete token on every request (`logger.info("JWT recibido: {}", jwt)`).
  This was found and fixed while building this catalog: it now logs
  only the first 8 characters, at `DEBUG` level, never `INFO`.
- **File contents** — import/export events log format, outcome, and
  count, never the uploaded/exported file's actual content.

## Correlation

Every log line automatically includes `correlationId` (see
`CorrelationIdFilter`), so a specific request's business event can be
matched against every other log line — including framework-level
ones — from the same request.
# Load Testing Baseline (k6)

This is a **snapshot for future comparison, not a performance
requirement or SLA**. It exists to catch gross future degradation
(e.g. an endpoint suddenly taking 10x longer) — not to validate the
app meets any particular throughput target. This is not performance
engineering.

Run locally against `docker-compose up --build` (prod profile), not
against a production deployment.

## How to run

```bash
k6 run scripts/k6/signin-load.js
k6 run scripts/k6/references-list-load.js
```

## Scenario 1: Repeated signin (`/api/auth/signin`)

5 virtual users, 30 seconds, one request per second per VU.

Since rate limiting was added in weeks 7-9 (#163), **most requests
here are expected to return 429** once the per-IP limit (5/min in
`prod`) is exceeded shortly after the run starts — this is correct
behavior being exercised, not a failure to fix.

| Metric | Value |
|---|---|
| Total requests | 150 |
| Checks succeeded | 100% (401 or 429, correlationId present) |
| p90 latency | 6.42 ms |
| p95 latency | 8.65 ms |
| Max latency | 360.92 ms |

## Scenario 2: Protected listing (`GET /api/references`)

5 virtual users, 30 seconds. `setup()` creates one test user and
seeds one reference, so the endpoint returns `200` with real content
(an empty list correctly returns `204 No Content` per existing
behavior — not representative of a realistic load scenario, so this
baseline explicitly seeds data first).

| Metric | Value |
|---|---|
| Total requests | 153 |
| Checks succeeded | 100% (200, correlationId present) |
| Failed requests | 0% |
| p90 latency | 9.64 ms |
| p95 latency | 10.97 ms |
| Max latency | 82.59 ms |

## Baseline date

2026-08-29, against the Fase 2 weeks 7-9 state (structured logging,
correlationId, business event catalog, and rate limiting all active).

## Notes for future comparison

- If a future run shows p95 latency significantly above these
  numbers (order of magnitude, not a few ms), investigate before
  assuming it's "normal drift."
- If scenario 1's checks start failing on status codes other than
  401/429, something changed in the auth or rate-limiting logic —
  investigate.
- This baseline does not cover import/export, sync, or admin
  endpoints — scope was deliberately kept to 2 scenarios per the
  Fase 2 plan.
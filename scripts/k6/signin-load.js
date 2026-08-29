import http from 'k6/http';
import { check, sleep } from 'k6';

// Baseline snapshot for the signin endpoint. This is NOT a
// performance requirement or SLA — it's a reference point to detect
// gross future degradation (e.g. an endpoint suddenly taking 10x
// longer). Since rate limiting was added in Fase 2 (weeks 7-9), most
// requests here are EXPECTED to return 429 once the per-IP limit is
// exceeded — that's correct behavior, not a bug to "fix" for this test.

export const options = {
    vus: 5,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8081';

export default function () {
    const payload = JSON.stringify({
        username: 'k6LoadTestUser',
        password: 'wrongPassword123',
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const res = http.post(`${BASE_URL}/api/auth/signin`, payload, params);

    check(res, {
        'status is 401 or 429 (both expected)': (r) => r.status === 401 || r.status === 429,
        'response has correlation id header': (r) => r.headers['X-Correlation-Id'] !== undefined,
    });

    sleep(1);
}
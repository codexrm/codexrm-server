import http from 'k6/http';
import { check, sleep } from 'k6';

// Baseline snapshot for a protected, authenticated listing endpoint.
// Same disclaimer as signin-load.js: reference point, not a
// requirement. Requires a valid JWT — obtained once during setup(),
// reused across all virtual users for the duration of the test.

export const options = {
    vus: 5,
    duration: '30s',
};

const BASE_URL = 'http://localhost:8081';

export function setup() {
    const uniqueUsername = `k6Perf${Date.now().toString().slice(-6)}`;

    const signupPayload = JSON.stringify({
        username: uniqueUsername,
        password: 'Test@123',
        email: `${uniqueUsername}@test.com`,
        name: 'K6',
        lastName: 'Perf',
        enabled: true,
    });

    const signupRes = http.post(`${BASE_URL}/api/auth/signup`, signupPayload, {
        headers: { 'Content-Type': 'application/json' },
    });

    const loginPayload = JSON.stringify({
        username: uniqueUsername,
        password: 'Test@123',
    });

const loginRes = http.post(`${BASE_URL}/api/auth/signin`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
});

console.log(`LOGIN status: ${loginRes.status}`);

const token = loginRes.json('token');

// Seed one reference so the endpoint returns 200 with real content,
  // instead of 204 No Content on an empty list (the app's correct
  // behavior for an empty result — but not representative of a
  // realistic load scenario).
 const referencePayload = JSON.stringify({
     title: 'K6 Load Test Reference',
     year: '2024',
     month: 'jan',
     note: 'Seeded for load testing',
     referenceType: 'ArticleReferenceDTO',
     author: 'Perf,Test',
     journal: 'Load Testing Journal',
     volume: '1',
     number: '1',
     pages: '1-10',
     issn: '1234-5678',
 });

 const createRes = http.post(`${BASE_URL}/api/references`, referencePayload, {
     headers: {
         'Content-Type': 'application/json',
         Authorization: `Bearer ${token}`,
     },
 });

 return { token };
}

export default function (data) {
    const params = {
        headers: {
            Authorization: `Bearer ${data.token}`,
        },
    };

    const res = http.get(`${BASE_URL}/api/references?page=0&size=10`, params);

    check(res, {
       'status is 200 with content': (r) => r.status === 200,
       'response has correlation id header': (r) => r.headers['X-Correlation-Id'] !== undefined,
   });

    sleep(1);
}
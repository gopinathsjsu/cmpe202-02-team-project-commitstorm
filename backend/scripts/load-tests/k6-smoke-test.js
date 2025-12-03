import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: Number(__ENV.VUS || 5),
  duration: __ENV.DURATION || '30s',
  thresholds: {
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TEST_EMAIL = __ENV.TEST_EMAIL || 'admin@demo.campusmarket.com';
const TEST_PASSWORD = __ENV.TEST_PASSWORD || 'demo123';

export default function () {
  const health = http.get(`${BASE_URL}/api/health`);
  check(health, {
    'health endpoint healthy': (res) => res.status === 200,
  });

  const loginPayload = JSON.stringify({
    email: TEST_EMAIL,
    password: TEST_PASSWORD,
  });
  const login = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(login, {
    'login succeeded': (res) => res.status === 200,
  });

  const token = login.json('token');
  if (token) {
    const authHeaders = { Authorization: `Bearer ${token}` };
    const listings = http.get(`${BASE_URL}/api/listings`, { headers: authHeaders });
    check(listings, {
      'listings reachable': (res) => res.status === 200,
    });
  }

  sleep(1);
}

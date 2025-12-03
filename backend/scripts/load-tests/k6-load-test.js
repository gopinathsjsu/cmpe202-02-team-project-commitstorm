import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TEST_EMAIL = __ENV.TEST_EMAIL || 'admin@demo.campusmarket.com';
const TEST_PASSWORD = __ENV.TEST_PASSWORD || 'demo123';
const ENABLE_WRITES = (__ENV.ENABLE_WRITES || 'false').toLowerCase() === 'true';

export const options = {
  scenarios: {
    readHeavy: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.READ_RATE || 20),
      timeUnit: '1s',
      duration: __ENV.READ_DURATION || '2m',
      preAllocatedVUs: Number(__ENV.READ_VUS || 10),
      exec: 'readScenario',
    },
    writeLight: {
      executor: 'ramping-arrival-rate',
      startRate: ENABLE_WRITES ? Number(__ENV.WRITE_START_RATE || 1) : 0,
      timeUnit: '1s',
      stages: ENABLE_WRITES
        ? [
            { target: Number(__ENV.WRITE_PEAK_RATE || 5), duration: '1m' },
            { target: Number(__ENV.WRITE_PEAK_RATE || 5), duration: '1m' },
            { target: 0, duration: '30s' },
          ]
        : [{ target: 0, duration: '1s' }],
      preAllocatedVUs: Number(__ENV.WRITE_VUS || 5),
      exec: 'createListingScenario',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1200'],
  },
};

export function setup() {
  const loginPayload = JSON.stringify({ email: TEST_EMAIL, password: TEST_PASSWORD });
  const login = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(login, { 'login ok': (res) => res.status === 200 });

  const token = login.json('token');
  const userId = login.json('id');

  const headers = { Authorization: `Bearer ${token}` };
  const categoriesRes = http.get(`${BASE_URL}/api/categories`, { headers });
  check(categoriesRes, { 'categories ok': (res) => res.status === 200 });
  const categories = categoriesRes.json();
  const categoryId = categories && categories.length ? categories[0].id : null;

  return { token, userId, categoryId };
}

export function readScenario(data) {
  const headers = { Authorization: `Bearer ${data.token}` };
  const listings = http.get(`${BASE_URL}/api/listings`, { headers });
  check(listings, { 'listings 200': (res) => res.status === 200 });

  const search = http.get(`${BASE_URL}/api/listings/search?searchTerm=laptop`, { headers });
  check(search, { 'search 200': (res) => res.status === 200 });

  sleep(0.5);
}

export function createListingScenario(data) {
  if (!ENABLE_WRITES || !data.categoryId) {
    sleep(1);
    return;
  }

  const payload = JSON.stringify({
    sellerId: data.userId,
    title: `Load Test Listing ${Date.now()}`,
    description: 'Automatically created during k6 run',
    price: 49.99,
    categoryId: data.categoryId,
    condition: 'GOOD',
    images: '[]',
    status: 'ACTIVE',
  });

  const headers = {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${data.token}`,
  };

  const response = http.post(`${BASE_URL}/api/listings`, payload, { headers });
  check(response, {
    'listing created': (res) => res.status === 201 || res.status === 400,
  });

  sleep(1);
}

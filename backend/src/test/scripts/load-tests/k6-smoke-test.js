// k6 smoke test for Campus Marketplace API
// Run: k6 run k6-smoke-test.js

import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 },  // Ramp up to 10 users
    { duration: '1m', target: 10 },  // Stay at 10 users
    { duration: '30s', target: 0 },  // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.01'],    // Less than 1% failures
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // Health check
  let healthRes = http.get(`${BASE_URL}/api/health`);
  check(healthRes, {
    'health check status is 200': (r) => r.status === 200,
    'health check response time < 200ms': (r) => r.timings.duration < 200,
  });

  // Get categories
  let categoriesRes = http.get(`${BASE_URL}/api/categories`);
  check(categoriesRes, {
    'categories status is 200': (r) => r.status === 200,
    'categories response has data': (r) => JSON.parse(r.body).length > 0,
  });

  // Get listings
  let listingsRes = http.get(`${BASE_URL}/api/listings`);
  check(listingsRes, {
    'listings status is 200': (r) => r.status === 200,
  });

  // Search listings
  let searchRes = http.get(`${BASE_URL}/api/listings/search?searchTerm=laptop`);
  check(searchRes, {
    'search status is 200': (r) => r.status === 200,
  });

  sleep(1);
}


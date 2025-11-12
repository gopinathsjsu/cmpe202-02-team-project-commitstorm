// k6 load test for Campus Marketplace API
// Run: k6 run --vus 50 --duration 2m k6-load-test.js

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const errorRate = new Rate('errors');

export const options = {
  stages: [
    { duration: '30s', target: 50 },  // Ramp up to 50 VUs
    { duration: '2m', target: 50 },   // Stay at 50 VUs for 2 minutes
    { duration: '30s', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
    http_req_failed: ['rate<0.05'],
    errors: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Demo credentials
const SELLER_EMAIL = 'seller@demo.campusmarket.com';
const SELLER_PASSWORD = 'demo123';
const BUYER_EMAIL = 'buyer@demo.campusmarket.com';
const BUYER_PASSWORD = 'demo123';

let sellerToken = null;
let buyerToken = null;
let categoryId = null;
let listingId = null;

export function setup() {
  // Login as seller
  const sellerLoginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: SELLER_EMAIL,
    password: SELLER_PASSWORD,
  }), {
    headers: { 'Content-Type': 'application/json' },
  });
  
  if (sellerLoginRes.status === 200) {
    sellerToken = JSON.parse(sellerLoginRes.body).token;
  }

  // Login as buyer
  const buyerLoginRes = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: BUYER_EMAIL,
    password: BUYER_PASSWORD,
  }), {
    headers: { 'Content-Type': 'application/json' },
  });
  
  if (buyerLoginRes.status === 200) {
    buyerToken = JSON.parse(buyerLoginRes.body).token;
  }

  // Get categories
  const categoriesRes = http.get(`${BASE_URL}/api/categories`);
  if (categoriesRes.status === 200) {
    const categories = JSON.parse(categoriesRes.body);
    if (categories.length > 0) {
      categoryId = categories[0].id;
    }
  }

  return { sellerToken, buyerToken, categoryId };
}

export default function (data) {
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${data.sellerToken}`,
  };

  // Health check
  let res = http.get(`${BASE_URL}/api/health`);
  check(res, {
    'health check': (r) => r.status === 200,
  }) || errorRate.add(1);

  // Get listings (most common operation)
  res = http.get(`${BASE_URL}/api/listings`);
  check(res, {
    'get listings': (r) => r.status === 200,
  }) || errorRate.add(1);

  // Search listings
  res = http.get(`${BASE_URL}/api/listings/search?searchTerm=test`);
  check(res, {
    'search listings': (r) => r.status === 200,
  }) || errorRate.add(1);

  // Advanced search
  if (data.categoryId) {
    res = http.get(`${BASE_URL}/api/listings/search/advanced?categoryId=${data.categoryId}&sortBy=newest&page=0&size=10`);
    check(res, {
      'advanced search': (r) => r.status === 200,
    }) || errorRate.add(1);
  }

  // Get categories
  res = http.get(`${BASE_URL}/api/categories`);
  check(res, {
    'get categories': (r) => r.status === 200,
  }) || errorRate.add(1);

  sleep(Math.random() * 2 + 1); // Random sleep between 1-3 seconds
}


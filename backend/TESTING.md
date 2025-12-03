# Testing Guide 

This document reflects the current test status for the Campus Marketplace backend (as of 2025-11-16).
It lists how to run tests, what tests currently exist, and the high-priority tests that are still missing.

---

## Quick Commands

- Run all tests (unit + any integration tests present):

```bash
cd backend
mvn test
```

- Run a focused set of unit tests (example):

```bash
cd backend
mvn -Dtest=CategoryServiceTest,CategoryControllerTest,JwtUtilTest -Dspotless.skip=true test
```

- Run specific test class:

```bash
mvn -Dtest=AuthServiceTest test
```

Note: Integration tests (if added) may use Testcontainers and require Docker running.

---

## Delivery guardrails (Definition of Ready / Definition of Done)

**Definition of Ready** (a story cannot enter sprint planning unless):
- Acceptance criteria describe observable behavior plus data/edge cases.
- API/DB contracts are documented (link to `API_DOCUMENTATION.md` or migration ID), and required credentials or feature flags are noted.
- Test strategy is captured: which suites (unit, integration, k6, Postman) must be touched and any new fixtures/data needed.
- Dependencies and owners are identified (e.g., S3 bucket ACL change, security review).

**Definition of Done** (a story is shippable only when):
- `mvn test` (unit + integration) passes locally and in CI; new tests are added where behavior changed.
- Coverage/regression impact considered—Jacoco delta reviewed for critical modules.
- Documentation updated: README/runbook/API docs plus changelog if external behavior changed.
- Non-functional checks pass: k6 smoke (and load if required) meet thresholds, monitoring/alerts adjusted if metrics move.
- Changes merged to the release branch with reviewer approval, deployment notes + rollback steps captured in the sprint log.

---

## Current test status (summary)

- Unit + integration tests present and passing (latest run): **286 automated tests**.
- Test sources now live under `src/test/java/com/campus/marketplace/{controller,service,exception,config,integration}`.
- Integration tests leverage Testcontainers (real MySQL) for end-to-end API flows.
- JaCoCo (coverage) is configured in `pom.xml`; see `target/site/jacoco/index.html` for full report.

Coverage highlights (from `backend/target/site/jacoco/index.html`):

- Instruction coverage: **35%**
- Branch coverage: **13%**
- Classes analyzed: **71**

Open the HTML report at `backend/target/site/jacoco/index.html` for full details.

---

## What unit tests exist now (representative list)

The repository currently contains and we ran the following test classes (non-exhaustive list):

- `JwtUtilTest`
- `CategoryServiceTest`, `CategoryControllerTest`
- `UserServiceTest`, `UserControllerTest`
- `ListingServiceTest`, `ListingControllerTest`
- `ReviewServiceTest`, `ReviewControllerTest`
- `WishlistServiceTest`, `WishlistControllerTest`
- `FollowServiceTest`, `FollowControllerTest`
- `GlobalExceptionHandlerTest`
- `UserDetailsServiceImplTest`
- `ReportServiceTest`, `ReportControllerTest`
- `MessageServiceTest`, `MessageControllerTest`
- `ImageControllerTest`, `S3ServiceTest`

## Integration Tests

- Suite lives under `src/test/java/com/campus/marketplace/integration` and covers Auth ➜ Listing ➜ Transaction happy paths against a disposable MySQL Testcontainer.
- Requires Docker running locally. The container is launched automatically when `mvn test` executes.
- To run everything (unit + integration):

```bash
cd backend
mvn test
```

- To skip the Testcontainer suite (useful on CI agents without Docker) pass `-DskipITs=true` which activates the `skip-integration-tests` Maven profile:

```bash
mvn test -DskipITs=true
```

These unit tests use JUnit5 + Mockito and MockMvc (for controller layer tests).

---

## Remaining test gaps / next steps

Now that controller/service units plus the core integration flows are covered, focus future effort on:

- **Media uploads (S3 + ImageController)**: add integration tests backed by LocalStack or WireMock to exercise presigned URL generation and image validation end to end.
- **Message & notification workflows**: expand beyond existing unit tests to cover real conversations between buyers/sellers (controller ➜ service ➜ repository) with WebSocket/SSE stubs if applicable.
- **Chatbot/OpenAI contract tests**: introduce a WireMock test double to validate prompt formatting and fallback behavior without calling the live API.
- **Security regression tests**: add Spring Security slice tests that assert role-based access for admin-only controllers plus JWT filter edge cases (expired tokens, malformed Authorization header).
- **DataInitializer guardrails**: add profile-specific tests ensuring seeding logic respects duplicates and encoded passwords when the component is enabled.


---

## Coverage / JaCoCo

- JaCoCo is configured in `pom.xml`. To generate the HTML coverage report run:

```bash
cd backend
mvn test jacoco:report
# Then open: backend/target/site/jacoco/index.html
```

---

## DataInitializer note

- `DataInitializer` is a `@Component` that implements `CommandLineRunner` and will run at application startup. It seeds an admin and sample users + categories by calling service methods.
- Production caution: it will run against whichever `spring.datasource` the app uses (the default in `application.yml` points to an AWS RDS endpoint). Consider toggling it off in production with `@Profile` or `@ConditionalOnProperty` if needed.

---

## Load & Smoke testing (k6 + Postman)

- **Postman**: `backend/src/main/resources/postman/Smoke_Test_Collection.json` exercises core endpoints. Import into Postman or run with Newman.
- **k6 smoke run**: lightweight health + login + listings check.

```bash
cd backend
k6 run scripts/load-tests/k6-smoke-test.js \
	--env BASE_URL=https://api.example.com \
	--env TEST_EMAIL=admin@demo.campusmarket.com \
	--env TEST_PASSWORD=demo123
```

- **k6 load run**: constant read load + optional write trickle. Writes stay disabled unless `ENABLE_WRITES=true` (to avoid polluting prod data).

```bash
k6 run scripts/load-tests/k6-load-test.js \
	--env BASE_URL=https://api.example.com \
	--env TEST_EMAIL=admin@demo.campusmarket.com \
	--env TEST_PASSWORD=demo123 \
	--env READ_RATE=25 \
	--env ENABLE_WRITES=true
```

Env knobs:

- `BASE_URL`, `TEST_EMAIL`, `TEST_PASSWORD`: identity used for login.
- `ENABLE_WRITES=true`: allows the script to create temporary listings (seller = logged-in user, requires existing categories).
- `VUS`, `DURATION`, `READ_RATE`, `WRITE_PEAK_RATE`, etc., let you scale scenarios without editing the scripts.

Both scripts exit non-zero if >5% of requests fail or the 95th percentile latency breaches thresholds, making them suitable for CI smoke/load gates.

---



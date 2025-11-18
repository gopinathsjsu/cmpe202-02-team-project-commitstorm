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

## Current test status (summary)

- Unit tests present and passing (latest full run): **162 tests passed**.
- There are currently **~17 unit test files** under `src/test/java/com/campus/marketplace` covering many controllers and services.
- No integration test source files are present in the current repository tree. There are historical integration test artifacts under `target/surefire-reports/` from earlier runs, but the source integration tests are not in source control at the moment.
- JaCoCo (coverage) is configured in `pom.xml` and a report has been generated.

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

These unit tests use JUnit5 + Mockito and MockMvc (for controller layer tests).

---

## Missing / High‑Priority tests (recommended order)

These are non-trivial classes that currently lack unit tests and should be prioritized.

High priority (write these first):

- `AuthService` — login, register, password hashing, token issuance, error paths. Mock `UserRepository`, `JwtUtil`, `PasswordEncoder`.
- `AuthController` — happy/error HTTP flows (validate status codes/payloads). Mock `AuthService`.
- `TransactionService` — transaction lifecycle (create, complete, cancel), validations and side-effects (messages/listing status). Mock repositories and any payment wrapper.
- `TransactionController` — request/response flows; validate mapping of service exceptions to HTTP codes.
- `MessageService` — composing/saving messages and system-message automation; mock `MessageRepository` and related entities.
- `MessageController` — endpoints that orchestrate messaging operations.
- `S3Service` — presigned URL generation and upload handling; mock AWS client wrapper.
- `ImageController` — upload and presigned endpoints that call `S3Service`/`ImageUtil`.
- `JwtAuthenticationFilter` — filter behavior for valid/invalid/expired tokens; mock `JwtUtil` and `UserDetailsService`.
- `SecurityConfig` — basic assertions about security beans and endpoint protection (small context test).
- `ChatbotSearchService` — external OpenAI/chatbot call mapping and fallback behavior.

Medium priority:

- `DataInitializer` (test its behavior in a safe way — ensure it uses `existsBy...` guards).
- `ImageUtil` (image helper functions validation).

Lower priority (skip for now or test via integration tests):

- DTOs (plain POJOs) and simple repository interfaces — these are typically exercised by service and integration tests.

---

## Integration Tests

- There are currently **no integration test source files** in `src/test/java/...` in the checked-out repository. However, previous runs produced integration test results in `target/surefire-reports/` (these are historical artifacts).
- If you want integration tests, add test classes under `src/test/java/com/campus/marketplace/integration/` and use Testcontainers (MySQL) and `@SpringBootTest` + `@AutoConfigureMockMvc`.

Example integration test base (pattern):

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public abstract class IntegrationTestBase {
    // Testcontainers setup for MySQL
}
```

Note: Integration tests will pull Docker images and take longer; run them separately when needed.

---

## Coverage / JaCoCo

- JaCoCo is configured in `pom.xml`. To generate the HTML coverage report run:

```bash
cd backend
mvn test jacoco:report
# Then open: backend/target/site/jacoco/index.html
```

I ran the report and summarized the main coverage metrics above in the "Current test status" section.

---

## DataInitializer note

- `DataInitializer` is a `@Component` that implements `CommandLineRunner` and will run at application startup. It seeds an admin and sample users + categories by calling service methods.
- Production caution: it will run against whichever `spring.datasource` the app uses (the default in `application.yml` points to an AWS RDS endpoint). Consider toggling it off in production with `@Profile` or `@ConditionalOnProperty` if needed.

---



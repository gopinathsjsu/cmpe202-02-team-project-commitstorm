# Testing Guide

This document provides comprehensive instructions for running and understanding the test suite for the Campus Marketplace application.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Test Structure](#test-structure)
4. [Running Tests](#running-tests)
5. [Test Types Explained](#test-types-explained)
6. [Writing New Tests](#writing-new-tests)
7. [Troubleshooting](#troubleshooting)
8. [CI/CD Integration](#cicd-integration)

## Prerequisites

### Required Software

1. **Java 17+**
   ```bash
   java -version  # Should show version 17 or higher
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version  # Should show version 3.6 or higher
   ```

3. **Docker** (Required for integration tests)
   ```bash
   docker --version  # Should show Docker version
   docker ps         # Should work without errors
   ```

### Docker Setup

Integration tests use **Testcontainers** which requires Docker to be running:

**macOS:**
```bash
# Start Docker Desktop
open -a Docker
```

**Linux:**
```bash
# Start Docker daemon
sudo systemctl start docker
# Or if using Docker Desktop
systemctl --user start docker-desktop
```

**Windows:**
- Start Docker Desktop from Start Menu

**Verify Docker is running:**
```bash
docker ps
# Should return empty list or running containers (not an error)
```

## Quick Start

### Run All Tests
```bash
cd backend
mvn test
```

### Run Specific Test
```bash
cd backend
mvn test -Dtest=AuthControllerIntegrationTest
```

### Run Tests with Coverage
```bash
cd backend
mvn test jacoco:report
# View report: open target/site/jacoco/index.html
```

## Test Structure

```
backend/src/test/java/com/campus/marketplace/
├── integration/                    # Integration tests
│   ├── IntegrationTestBase.java   # Base class with Testcontainers setup
│   ├── AuthControllerIntegrationTest.java
│   ├── TransactionControllerIntegrationTest.java
│   ├── MessageControllerIntegrationTest.java
│   ├── ReviewControllerIntegrationTest.java
│   ├── ListingControllerIntegrationTest.java
│   ├── ListingIntegrationTest.java
│   ├── TransactionIntegrationTest.java
│   └── WishlistIntegrationTest.java
├── service/                        # Service unit tests
│   ├── AuthServiceTest.java
│   ├── TransactionServiceTest.java
│   └── MessageServiceTest.java
└── security/                       # Security tests
    └── SecurityTest.java
```

## Running Tests

### Run All Tests
```bash
cd backend
mvn test
```

**Expected Output:**
- All tests should pass
- Testcontainers will pull MySQL image on first run
- Each test class runs in isolation

### Run Tests by Category

#### Integration Tests Only
```bash
mvn test -Dtest="com.campus.marketplace.integration.*"
```

#### Unit Tests Only
```bash
mvn test -Dtest="com.campus.marketplace.service.*Test"
```

#### Security Tests Only
```bash
mvn test -Dtest="com.campus.marketplace.security.*"
```

### Run Specific Test Class
```bash
# Integration tests
mvn test -Dtest=AuthControllerIntegrationTest
mvn test -Dtest=TransactionControllerIntegrationTest
mvn test -Dtest=MessageControllerIntegrationTest
mvn test -Dtest=ReviewControllerIntegrationTest
mvn test -Dtest=ListingControllerIntegrationTest

# Unit tests
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=TransactionServiceTest
mvn test -Dtest=MessageServiceTest

# Security tests
mvn test -Dtest=SecurityTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=AuthControllerIntegrationTest#testRegister_Success
```

### Skip Tests
```bash
mvn clean install -DskipTests
```

### Run Tests with Maven Profile
```bash
# If you have test profiles configured
mvn test -Ptest-profile
```

## Test Types Explained

### 1. Integration Tests

**Location:** `backend/src/test/java/com/campus/marketplace/integration/`

**Framework:** JUnit 5 + Testcontainers + MockMvc + Spring Boot Test

**Purpose:** Test complete request/response cycles through controllers with real database

**Key Characteristics:**
- Extends `IntegrationTestBase` which sets up MySQL container
- Uses `@AutoConfigureMockMvc` for HTTP testing
- Uses `@Transactional` for automatic rollback
- Tests run against real MySQL database in Docker
- Database is automatically created and cleaned up

**Example:**
```java
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testRegister_Success() throws Exception {
        // Test HTTP request/response
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }
}
```

**What Gets Tested:**
- HTTP endpoints (GET, POST, PUT, DELETE, PATCH)
- Request validation
- Response status codes and content
- Database operations
- Business logic integration
- Automatic message sending
- Transaction flows

### 2. Unit Tests

**Location:** `backend/src/test/java/com/campus/marketplace/service/`

**Framework:** JUnit 5 + Mockito

**Purpose:** Test service methods in isolation with mocked dependencies

**Key Characteristics:**
- Uses `@ExtendWith(MockitoExtension.class)`
- Mocks all dependencies (`@Mock`)
- Injects service under test (`@InjectMocks`)
- Fast execution (no database, no HTTP)
- Tests business logic and error handling

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private AuthService authService;
    
    @Test
    void testAuthenticate_Success() {
        // Setup mocks
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encoded"))
            .thenReturn(true);
        
        // Test service method
        User result = authService.authenticate("test@example.com", "password");
        
        // Verify
        assertNotNull(result);
        verify(userRepository).findByEmail("test@example.com");
    }
}
```

**What Gets Tested:**
- Service method logic
- Input validation
- Error handling
- Business rules
- Exception scenarios

### 3. Security Tests

**Location:** `backend/src/test/java/com/campus/marketplace/security/`

**Framework:** JUnit 5 + Testcontainers + MockMvc

**Purpose:** Test authentication, authorization, and security features

**Key Characteristics:**
- Tests JWT token validation
- Tests public vs protected endpoints
- Tests role-based access control
- Tests inactive user restrictions

**Example:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class SecurityTest {
    
    @Test
    void testProtectedEndpoints_RequireAuth() throws Exception {
        // Try without token
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
```

**What Gets Tested:**
- JWT token generation and validation
- Public endpoint access
- Protected endpoint authorization
- Role-based access (USER vs ADMIN)
- Inactive user restrictions
- Token expiration handling

## Writing New Tests

### Adding an Integration Test

1. **Create test class:**
```java
package com.campus.marketplace.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
public class MyControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testMyEndpoint() throws Exception {
        // Your test code
    }
}
```

2. **Key annotations:**
   - `@AutoConfigureMockMvc` - Enables MockMvc
   - `@Transactional` - Auto-rollback after each test
   - Extend `IntegrationTestBase` - Gets MySQL container setup

### Adding a Unit Test

1. **Create test class:**
```java
package com.campus.marketplace.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    
    @Mock
    private MyRepository myRepository;
    
    @InjectMocks
    private MyService myService;
    
    @Test
    void testMyMethod() {
        // Your test code
    }
}
```

2. **Key annotations:**
   - `@ExtendWith(MockitoExtension.class)` - Enables Mockito
   - `@Mock` - Creates mock dependencies
   - `@InjectMocks` - Injects mocks into service

## Troubleshooting

### Docker Not Running

**Error:**
```
Could not find a valid Docker environment
```

**Solution:**
```bash
# Start Docker
# macOS:
open -a Docker

# Linux:
sudo systemctl start docker

# Verify:
docker ps
```

### Testcontainers Can't Pull Image

**Error:**
```
Failed to pull image: mysql:8.0
```

**Solution:**
```bash
# Manually pull the image
docker pull mysql:8.0

# Or check internet connection
docker pull hello-world
```

### Port Already in Use

**Error:**
```
Port 3306 is already in use
```

**Solution:**
```bash
# Testcontainers uses random ports, but if issue persists:
# Find process using port
lsof -i :3306  # macOS/Linux
netstat -ano | findstr :3306  # Windows

# Stop conflicting service or container
docker ps | grep mysql
docker stop <container-id>
```

### Tests Fail with Database Errors

**Error:**
```
Table 'campusMarketTest.users' doesn't exist
```

**Solution:**
1. Check Flyway is enabled in test configuration
2. Verify migration files exist in `src/main/resources/db/migration/`
3. Check migration file naming: `V1__description.sql`
4. Ensure migrations run before tests (they should automatically)

### Tests Are Slow

**Possible Causes:**
1. First run downloads MySQL image (one-time)
2. Docker container startup time
3. Too many integration tests

**Solutions:**
```bash
# Run only unit tests (faster)
mvn test -Dtest="com.campus.marketplace.service.*Test"

# Use Testcontainers reuse (already enabled)
# Containers are reused between test runs

# Run tests in parallel (if configured)
mvn test -T 4
```

### Out of Memory Errors

**Error:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx2048m -Xms1024m"
mvn test
```

### Test Data Conflicts

**Issue:** Tests interfere with each other

**Solution:**
- All integration tests use `@Transactional` for automatic rollback
- Each test should create its own test data
- Use unique identifiers (UUIDs) for test entities
- Clean up in `@BeforeEach` or `@AfterEach` if needed

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      docker:
        image: docker:latest
        options: --privileged
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Start Docker
        run: |
          sudo systemctl start docker
          docker ps
      
      - name: Run Tests
        run: |
          cd backend
          mvn test
      
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: backend/target/surefire-reports/
```

### Jenkins Example

```groovy
pipeline {
    agent any
    
    stages {
        stage('Test') {
            steps {
                sh '''
                    cd backend
                    mvn test
                '''
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }
    }
}
```

### Requirements for CI

1. **Docker must be available**
   - GitHub Actions: Use `services: docker`
   - Jenkins: Install Docker plugin
   - GitLab CI: Use `docker:dind` service

2. **Java 17+**
   - Most CI platforms support this

3. **Maven 3.6+**
   - Usually pre-installed or easy to add

4. **Testcontainers Configuration**
   - Works automatically in most CI environments
   - May need to configure Docker-in-Docker for some platforms

## Test Coverage

### Current Coverage

The test suite covers:

✅ **Controllers:**
- AuthController (register, login, logout, current user)
- TransactionController (request-to-buy, mark-sold, reject)
- MessageController (send, get, unread, mark as read)
- ReviewController (create, get reviews)
- ListingController (CRUD, search, filters)

✅ **Services:**
- AuthService (authentication, registration)
- TransactionService (transaction flow, message automation)
- MessageService (messaging operations)

✅ **Security:**
- JWT authentication and validation
- Authorization checks
- Role-based access control
- Inactive user handling

✅ **Business Logic:**
- Transaction flow (request → accept/reject)
- Automatic message sending
- Listing status transitions
- Review creation and validation

### Generating Coverage Reports

```bash
# Add jacoco plugin to pom.xml (if not already present)
# Then run:
mvn test jacoco:report

# View report
open backend/target/site/jacoco/index.html
```

## Best Practices

1. **Isolation:** Each test should be independent
2. **Cleanup:** Use `@Transactional` for automatic rollback
3. **Naming:** Use descriptive test method names (`testMethodName_Scenario_ExpectedResult`)
4. **Assertions:** Use specific assertions, not just `assertTrue`
5. **Mocks:** Only mock external dependencies, not the class under test
6. **Speed:** Prefer unit tests for fast feedback, integration tests for full coverage
7. **Data:** Create test data in `@BeforeEach`, clean up in `@AfterEach` if needed

## Additional Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)


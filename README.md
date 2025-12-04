# Campus Marketplace - Spring Boot Application

A comprehensive campus marketplace application built with Spring Boot that allows students to buy and sell items within their campus community.

## Features

- **User Management**: User registration, authentication, and role-based access control
- **Category Management**: Organize items into categories
- **Listing Management**: Create, update, and manage product listings
- **Wishlist**: Save items for later purchase
- **Messaging System**: Communication between buyers and sellers
- **Transaction Management**: Handle purchases and payments
- **Review System**: Rate and review completed transactions
- **Reporting System**: Report inappropriate content or users
- **Admin Panel**: Administrative functions for moderators

## Technology Stack

- **Backend**: Spring Boot 3.3.2
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **Migration**: Flyway
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven
- **Java Version**: 17

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Git

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd cmpe202-02-team-project-commitstorm
```

### 2. Database Setup
The application is configured to use AWS RDS MySQL database:
- **Host**: commitstorm.c3k8w4gacaeh.us-west-2.rds.amazonaws.com
- **Database**: campusMarket
- **Username**: admin
- **Password**: commitstorm

The database connection is already configured in `backend/src/main/resources/application.yml`. No additional setup is required.

### 3. Run the Application

#### Option 1: Using the startup script
```bash
cd backend
./start.sh
```

#### Option 2: Using Maven directly
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

#### Option 3: Using Docker (for local development only)
```bash
cd backend
docker-compose up
```

## API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html (local) / http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com/swagger-ui.html (production)
- **API Docs**: http://localhost:8080/api-docs (local) / http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com/api-docs (production)
- **Health Check**: http://localhost:8080/health (local) / http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com/health (production)

## API Endpoints

The application provides REST APIs for all major operations:

### Core Entities
- **Users**: `/api/users` - User management
- **Categories**: `/api/categories` - Category management
- **Listings**: `/api/listings` - Product listings
- **Images**: `/api/images` - Image upload and management
  - `POST /api/images/presigned-url` - Get presigned URL for upload
  - `POST /api/images/confirm-upload` - Confirm upload and make public
  - `GET /api/images/presigned-get-url` - Get presigned URL for viewing
- **Wishlist**: `/api/wishlist` - User wishlists
- **Messages**: `/api/messages` - Communication system
- **Transactions**: `/api/transactions` - Purchase management
- **Reviews**: `/api/reviews` - Rating and review system
- **Reports**: `/api/reports` - Content moderation

For detailed API documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

## Database Schema

The application uses the following main entities:
- `users` - User accounts and profiles
- `categories` - Product categories
- `listings` - Product listings
- `wishlist` - User wishlist items
- `messages` - Communication between users
- `reports` - Content and user reports
- `transactions` - Purchase transactions
- `reviews` - Product and seller reviews

## Sample Data

The application automatically initializes with sample data:
- Admin user: `admin@campusmarket.com`
- Sample users: `john.doe@university.edu`, `jane.smith@university.edu`
- Predefined categories: Electronics, Books, Clothing, Furniture, etc.

## Configuration

Key configuration options in `application.yml`:
- Database connection settings
- Server port (default: 8080)
- Logging levels
- Swagger documentation settings

## Development

### Project Structure
```
backend/
├── src/main/java/com/campus/marketplace/
│   ├── entity/          # JPA entities
│   ├── repository/      # Data access layer
│   ├── service/         # Business logic
│   ├── controller/      # REST controllers
│   ├── dto/            # Data transfer objects
│   ├── exception/      # Exception handling
│   └── config/         # Configuration classes
├── src/main/resources/
│   ├── application.yml  # Application configuration
│       ├── migration/   # Database migrations
│       └── testing/     # Database testing scripts
└── pom.xml              # Maven dependencies
```

### Adding New Features
1. Create entity classes in `entity/` package
2. Add repository interfaces in `repository/` package
3. Implement business logic in `service/` package
4. Create REST controllers in `controller/` package
5. Add DTOs in `dto/` package for request/response handling

## Testing

The application includes a comprehensive test suite with unit tests, integration tests, and security tests.

### Test Prerequisites

Before running tests, ensure you have:
- **Java 17** or higher
- **Maven 3.6** or higher
- **Docker** (required for Testcontainers integration tests)
  - Testcontainers uses Docker to spin up MySQL containers for integration tests
  - Make sure Docker is running before executing tests

### Running Tests

#### Run All Tests
```bash
cd backend
mvn test

# For clean output (less verbose):
mvn test -q
```

#### Run Specific Test Class
```bash
# Integration tests
mvn test -Dtest=AuthControllerIntegrationTest
mvn test -Dtest=TransactionControllerIntegrationTest
mvn test -Dtest=MessageControllerIntegrationTest

# Unit tests
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=TransactionServiceTest
mvn test -Dtest=MessageServiceTest

# Security tests
mvn test -Dtest=SecurityTest
```

#### Run Tests by Package
```bash
# All integration tests
mvn test -Dtest="com.campus.marketplace.integration.*"

# All service unit tests
mvn test -Dtest="com.campus.marketplace.service.*Test"

# All security tests
mvn test -Dtest="com.campus.marketplace.security.*"
```

#### Run Tests with Coverage
```bash
# Generate test coverage report (requires jacoco plugin)
mvn test jacoco:report
# View report at: backend/target/site/jacoco/index.html
```

### Test Structure

The test suite is organized as follows:

```
backend/src/test/java/com/campus/marketplace/
├── integration/              # Integration tests (use Testcontainers)
│   ├── IntegrationTestBase.java
│   ├── AuthControllerIntegrationTest.java
│   ├── TransactionControllerIntegrationTest.java
│   ├── MessageControllerIntegrationTest.java
│   ├── ReviewControllerIntegrationTest.java
│   ├── ListingControllerIntegrationTest.java
│   ├── ListingIntegrationTest.java
│   ├── TransactionIntegrationTest.java
│   └── WishlistIntegrationTest.java
├── service/                 # Service unit tests (use Mockito)
│   ├── AuthServiceTest.java
│   ├── TransactionServiceTest.java
│   └── MessageServiceTest.java
└── security/                # Security and authorization tests
    └── SecurityTest.java
```

### Test Types

#### 1. Integration Tests
- **Framework**: JUnit 5 + Testcontainers + MockMvc
- **Database**: Uses Testcontainers to spin up MySQL 8.0 containers
- **Scope**: Tests full request/response cycle through controllers
- **Location**: `backend/src/test/java/com/campus/marketplace/integration/`

**Key Features:**
- Tests run against real MySQL database in Docker containers
- Database is automatically created and cleaned up per test
- Tests verify complete transaction flows including automatic message sending
- All database operations are transactional and rolled back after each test

**Example:**
```java
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIntegrationTest extends IntegrationTestBase {
    // Tests authentication endpoints with real database
}
```

#### 2. Unit Tests
- **Framework**: JUnit 5 + Mockito
- **Scope**: Tests individual service methods in isolation
- **Location**: `backend/src/test/java/com/campus/marketplace/service/`

**Key Features:**
- Uses mocks to isolate service logic
- Fast execution (no database required)
- Tests business logic and error handling

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthService authService;
    // Tests service methods with mocked dependencies
}
```

#### 3. Security Tests
- **Framework**: JUnit 5 + Testcontainers + MockMvc
- **Scope**: Tests JWT authentication, authorization, and role-based access
- **Location**: `backend/src/test/java/com/campus/marketplace/security/`

**Key Features:**
- Tests public vs protected endpoints
- Validates JWT token handling
- Tests role-based access control
- Verifies inactive user restrictions

### Test Coverage

The test suite covers:

✅ **Controllers** (Integration Tests):
- AuthController (register, login, logout, current user)
- TransactionController (request-to-buy, mark-sold, reject)
- MessageController (send, get, unread messages, mark as read)
- ReviewController (create, get reviews)
- ListingController (CRUD, search, filters)

✅ **Services** (Unit Tests):
- AuthService (authentication, registration)
- TransactionService (transaction flow, message automation)
- MessageService (messaging operations)

✅ **Security**:
- JWT authentication and validation
- Authorization checks
- Role-based access control
- Inactive user handling

✅ **Business Logic**:
- Transaction flow (request → accept/reject)
- Automatic message sending for transactions and reviews
- Listing status transitions
- Review creation and validation

### Troubleshooting Tests

#### Docker Not Running
If you see errors like "Could not find a valid Docker environment":
```bash
# Start Docker Desktop or Docker daemon
# On macOS:
open -a Docker

# On Linux:
sudo systemctl start docker
```

#### Testcontainers Connection Issues
If tests fail with connection errors:
```bash
# Verify Docker is accessible
docker ps

# Check Testcontainers configuration
# Tests use MySQL 8.0 container with reuse enabled
```

#### Port Conflicts
If you see port binding errors:
```bash
# Tests use random ports, but if issues persist:
# Check for running MySQL instances
docker ps | grep mysql

# Stop conflicting containers
docker stop <container-id>
```

#### Database Migration Issues
If Flyway migrations fail in tests:
```bash
# Tests automatically run migrations
# If issues occur, check:
# 1. Flyway is enabled in test configuration
# 2. Migration files are in src/main/resources/db/migration/
```

### Continuous Integration

Tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run Tests
  run: |
    cd backend
    mvn test
```

**Requirements for CI:**
- Docker must be available in CI environment
- Testcontainers will automatically pull MySQL image if needed
- Tests are isolated and can run in parallel

### Manual API Testing

For manual API testing, you can also use:
- **Swagger UI**: http://localhost:8080/swagger-ui.html (local) / http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com/swagger-ui.html (production)
- **Postman Collection**: `Campus Marketplace API.postman_collection.json`
- **cURL commands**: See API_DOCUMENTATION.md

### Detailed Testing Documentation

For comprehensive testing instructions, see **[TESTING.md](backend/TESTING.md)** which includes:
- Detailed setup instructions
- Test structure explanation
- Writing new tests guide
- Troubleshooting common issues
- CI/CD integration examples

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is part of CMPE 202 coursework.

## Team

- Sonali Lonkar
- Sofia Silva
- Shivani Jariwala
- Alex

## 🚀 Production Deployment

### AWS Architecture Overview

```
┌─────────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│   Internet      │────▶│  Application Load    │────▶│   Target Group   │
│   Users         │     │  Balancer (ALB)      │     │   (Port 8080)    │
└─────────────────┘     └──────────────────────┘     └─────────────────┘
                                │                                 │
                                │                                 ▼
                                │                       ┌─────────────────┐
                                │                       │   EC2 Instance  │
                                │                       │   (Docker)      │
                                ▼                       │                 │
                      ┌──────────────────────┐         │  ┌────────────┐ │
                      │   Security Groups    │         │  │ Spring Boot │ │
                      │                      │         │  │   App       │ │
                      │ • ALB Security Group │         │  │ (Port 8080) │ │
                      │   - Inbound: 80,443 │         │  └────────────┘ │
                      │   - Outbound: All    │         └─────────────────┘
                      │                      │
                      │ • EC2 Security Group │                   │
                      │   - Inbound: 8080   │                   ▼
                      │     (from ALB only)  │         ┌─────────────────┐
                      │   - Outbound: All    │         │   AWS RDS       │
                      └──────────────────────┘         │   MySQL 8.0     │
                                                      │   Database      │
                                                      └─────────────────┘
```

### Infrastructure Components

#### **Application Load Balancer (ALB)**
- **Type**: Application Load Balancer
- **Listeners**: HTTP (80) and HTTPS (443)
- **Target Groups**: 
  - Protocol: HTTP
  - Port: 8080
  - Health Check: `/api/health`
  - Healthy threshold: 2
  - Unhealthy threshold: 2
  - Timeout: 5 seconds
  - Interval: 30 seconds

#### **Security Groups**
- **ALB Security Group**:
  - Inbound: HTTP (80), HTTPS (443) from 0.0.0.0/0
  - Outbound: All traffic to 0.0.0.0/0

- **EC2 Security Group**:
  - Inbound: HTTP (8080) from ALB Security Group only
  - Outbound: All traffic to 0.0.0.0/0

#### **EC2 Instance**
- **AMI**: Amazon Linux 2 or Ubuntu
- **Instance Type**: t3.micro (for development) or t3.small+ (for production)
- **Storage**: 20GB+ EBS
- **Docker**: Pre-installed and configured

#### **RDS MySQL Database**
- **Engine**: MySQL 8.0
- **Instance Class**: db.t3.micro (development) or db.t3.small+ (production)
- **Storage**: 20GB+ with auto-scaling
- **Multi-AZ**: Enabled for production
- **Backup**: Automated daily backups

### Deployment Process

#### Prerequisites
- AWS Account with appropriate permissions
- EC2 instance with Docker installed
- RDS MySQL database created
- ALB configured with target groups
- Security groups properly configured
- Domain name (optional, for HTTPS)

#### Environment Variables
Create a `.env` file in the `backend/` directory:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-endpoint.rds.amazonaws.com:3306/campusMarket?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true
SPRING_DATASOURCE_USERNAME=your-db-username
SPRING_DATASOURCE_PASSWORD=your-db-password

# AWS S3 Configuration (for image uploads)
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
AWS_S3_BUCKET_NAME=your-s3-bucket-name
AWS_REGION=us-west-2

# OpenAI Configuration (for chatbot)
OPENAI_API_KEY=your-openai-api-key

# Server Configuration
SERVER_PORT=8080
```

### Quick Deploy

1. **Clone and Setup**:
   ```bash
   git clone <repository-url>
   cd cmpe202-02-team-project-commitstorm/backend
   cp .env.example .env
   # Edit .env with your actual credentials
   ```

2. **Deploy to EC2**:
   ```bash
   # Copy files to EC2 instance
   scp -i your-key.pem -r . ubuntu@your-ec2-instance:/home/ubuntu/app/

   # SSH into EC2 and deploy
   ssh -i your-key.pem ubuntu@your-ec2-instance
   cd /home/ubuntu/app
   chmod +x scripts/deploy.sh
   ./scripts/deploy.sh
   ```

3. **Verify Deployment**:
   ```bash
   # Check container status
   docker ps

   # Check application health
   curl http://localhost:8080/api/health

   # Check ALB health
   curl http://your-alb-url.amazonaws.com/api/health
   ```

### Deployment Scripts

#### Docker-based Deployment Script
Create `backend/scripts/deploy.sh`:

```bash
#!/bin/bash
set -e

echo "🚀 Starting Campus Marketplace Deployment..."

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | xargs)
fi

# Stop existing container
echo "🛑 Stopping existing containers..."
docker stop campus_api || true
docker rm campus_api || true

# Pull latest image (if using registry)
# docker pull your-registry/campus-api:latest

# Build application
echo "🔨 Building application..."
./mvnw clean package -DskipTests

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t campus-api:latest .

# Run new container
echo "🚀 Starting new container..."
docker run -d \
  --name campus_api \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="$SPRING_DATASOURCE_URL" \
  -e SPRING_DATASOURCE_USERNAME="$SPRING_DATASOURCE_USERNAME" \
  -e SPRING_DATASOURCE_PASSWORD="$SPRING_DATASOURCE_PASSWORD" \
  -e AWS_ACCESS_KEY_ID="$AWS_ACCESS_KEY_ID" \
  -e AWS_SECRET_ACCESS_KEY="$AWS_SECRET_ACCESS_KEY" \
  -e AWS_S3_BUCKET_NAME="$AWS_S3_BUCKET_NAME" \
  -e AWS_REGION="$AWS_REGION" \
  -e OPENAI_API_KEY="$OPENAI_API_KEY" \
  campus-api:latest

# Wait for health check
echo "⏳ Waiting for application to start..."
sleep 30

# Health check
if curl -f http://localhost:8080/api/health > /dev/null; then
    echo "✅ Deployment successful!"
    echo "📊 Application is running at: http://localhost:8080"
else
    echo "❌ Health check failed!"
    docker logs campus_api
    exit 1
fi
```

#### AWS Cloud Infrastructure (Manual Console Deployment)
The application is deployed on AWS using manually configured resources through the AWS Management Console:

**Infrastructure Components:**
- **Application Load Balancer (ALB)**: `alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com`
- **EC2 Instance**: Amazon Linux 2 with Docker, running Spring Boot application
- **RDS MySQL Database**: MySQL 8.0 instance for data persistence
- **S3 Bucket**: For image storage with presigned URLs
- **Security Groups**: Properly configured for secure communication
- **VPC**: Isolated network with public/private subnets

**Key Configurations:**
- ALB forwards HTTP traffic to EC2 instance on port 8080
- Health checks configured for `/api/health` endpoint
- Database security group restricts access to EC2 instances only
- Application runs in Docker container with environment variables

### Monitoring & Observability

#### Health Checks
- **Application Health**: `/api/health` endpoint
- **Database Connectivity**: Automatic in health check
- **ALB Target Health**: Configured in target group

#### Logs
```bash
# Application logs
docker logs campus_api

# ALB Access Logs (CloudWatch)
aws logs tail /aws/elasticloadbalancing/campus-marketplace-alb --follow

# System logs
journalctl -u docker -f
```

#### Metrics to Monitor
- **ALB Metrics**: Request count, response time, error rates
- **EC2 Metrics**: CPU utilization, memory usage, network I/O
- **RDS Metrics**: Database connections, query latency, storage usage
- **Application Metrics**: Custom business metrics via Spring Boot Actuator

### Troubleshooting

#### Common Issues

**ALB Health Check Failures**:
```bash
# Check application health
curl http://localhost:8080/api/health

# Check target group health
aws elbv2 describe-target-health --target-group-arn your-target-group-arn
```

**Database Connection Issues**:
```bash
# Test database connectivity from EC2
mysql -h your-rds-endpoint -u admin -p campusMarket

# Check security group rules
aws ec2 describe-security-groups --group-ids your-rds-sg-id
```

**Container Deployment Issues**:
```bash
# Check container logs
docker logs campus_api

# Check container status
docker ps -a

# Restart container
docker restart campus_api
```

### Scaling Considerations

#### Horizontal Scaling
- Add more EC2 instances to target group
- Use Auto Scaling Groups for automatic scaling
- Implement session affinity if needed

#### Vertical Scaling
- Upgrade EC2 instance types
- Increase RDS instance size
- Optimize application performance

#### Database Scaling
- Read replicas for read-heavy workloads
- Connection pooling
- Query optimization and indexing

### Documentation

- **[Production Deployment Guide](backend/PRODUCTION_DEPLOYMENT.md)** - Complete deployment checklist
- **[Runbook](backend/RUNBOOK.md)** - Operations and troubleshooting guide
- **[Backup & Rollback](backend/BACKUP_ROLLBACK.md)** - Backup procedures and rollback steps
- **[API Documentation](API_DOCUMENTATION.md)** - Detailed API specifications
- **[Testing Guide](backend/TESTING.md)** - Comprehensive testing documentation

### Demo Accounts

After running V5 migration:
- **Admin**: `admin@campusmarket.com` / `demo123`
- **Sample Users**: `john.doe@university.edu` / `demo123`, `jane.smith@university.edu` / `demo123`

### Testing

#### Postman Collection
- **Full Collection**: `Campus Marketplace API.postman_collection.json`
- **Smoke Tests**: `backend/postman/Smoke_Test_Collection.json`
- Import into Postman and set `base_url` variable

#### Load Testing
```bash
# Install k6
brew install k6

# Run smoke test (basic functionality)
k6 run backend/scripts/load-tests/k6-smoke-test.js

# Run load test (performance under load)
k6 run --vus 50 --duration 2m backend/scripts/load-tests/k6-load-test.js

# For production testing, set BASE_URL environment variable:
# BASE_URL=https://your-alb-url.amazonaws.com k6 run backend/scripts/load-tests/k6-smoke-test.js
```

#### Demo Script
```bash
./backend/scripts/demo-script.sh
```

## ⚠️ CRITICAL: Database Isolation Issue Fixed

**Problem**: Integration tests were running against the PRODUCTION database and deleting all data with cleanup scripts.

**Root Cause**: 
- Tests configured to use production RDS instead of isolated test database
- `@Sql(cleanup.sql)` runs before/after each test, truncating ALL tables
- No Testcontainers or database isolation implemented

**Solution Applied**:
- ✅ **Fixed**: Integration tests now use Testcontainers with isolated MySQL instances
- ✅ **Created**: `reset-demo.sh` script to restore demo data via API
- ✅ **Added**: Comprehensive demo data script for presentation

**To restore demo data after test runs**:
```bash
cd backend
./scripts/reset-demo.sh
```

## 📊 Monitoring

### Health Check
```bash
# Quick health check
curl http://your-domain/api/health

# Comprehensive health check script
cd backend/scripts
./health-check.sh local    # For local development
./health-check.sh prod     # For production ALB
```

### Logs
```bash
# Application logs
docker logs campus_api

# Application logs (follow)
docker logs campus_api --tail 100 -f

# ALB Access Logs (CloudWatch)
aws logs tail /aws/elasticloadbalancing/campus-marketplace-alb --follow

# System logs
journalctl -u docker -f
```

## 🔧 Maintenance

### Application Updates
```bash
# Pull latest changes
git pull

# Deploy updates
cd backend
./scripts/deploy.sh
```

### Reset Demo Data
```bash
./backend/scripts/reset-demo.sh
```

### Docker Management
```bash
# View running containers
docker ps

# View all containers
docker ps -a

# Restart application
docker restart campus_api

# View logs
docker logs campus_api

# Clean up unused images
docker image prune -f
```

## Support

For questions or issues, please contact the development team or create an issue in the repository.

## 📚 Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html (local) / http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com/swagger-ui.html (production)
- **Postman Collection**: Import `Campus Marketplace API.postman_collection.json`
- **Authentication Guide**: [AUTHENTICATION.md](backend/AUTHENTICATION.md)

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

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/health

## API Endpoints

The application provides REST APIs for all major operations:

### Core Entities
- **Users**: `/api/users` - User management
- **Categories**: `/api/categories` - Category management
- **Listings**: `/api/listings` - Product listings
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

The application includes comprehensive API endpoints that can be tested using:
- Swagger UI (http://localhost:8080/swagger-ui.html)
- Postman or similar API testing tools
- cURL commands

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

### Architecture

```
┌─────────────┐
│   Client    │
│  (React)    │
└──────┬──────┘
       │ HTTPS
       ▼
┌─────────────┐
│   Nginx     │
│ (Port 80)   │
└──────┬──────┘
       │
       ▼
┌─────────────┐      ┌─────────────┐
│   Spring    │◄────►│   MySQL     │
│    Boot     │      │     RDS     │
│ (Port 8080) │      │             │
└─────────────┘      └─────────────┘
     EC2                  AWS RDS
```

### Quick Deploy

1. **Prerequisites**:
   - EC2 instance with Docker
   - RDS MySQL database
   - Security groups configured

2. **Deploy**:
   ```bash
   cd backend
   cp .env.example .env
   # Edit .env with your RDS credentials
   ./scripts/deploy.sh
   ```

3. **Verify**:
   ```bash
   curl http://localhost:8080/api/health
   ```

### Documentation

- **[Production Deployment Guide](backend/PRODUCTION_DEPLOYMENT.md)** - Complete deployment checklist
- **[Runbook](backend/RUNBOOK.md)** - Operations and troubleshooting guide
- **[Backup & Rollback](backend/BACKUP_ROLLBACK.md)** - Backup procedures and rollback steps
- **[API Documentation](API_DOCUMENTATION.md)** - Detailed API specifications

### Demo Accounts

After running V5 migration:
- **Admin**: `admin@demo.campusmarket.com` / `demo123`
- **Seller**: `seller@demo.campusmarket.com` / `demo123`
- **Buyer**: `buyer@demo.campusmarket.com` / `demo123`

### Testing

#### Postman Collection
- **Full Collection**: `Campus Marketplace API.postman_collection.json`
- **Smoke Tests**: `backend/postman/Smoke_Test_Collection.json`
- Import into Postman and set `base_url` variable

#### Load Testing
```bash
# Install k6: https://k6.io/docs/getting-started/installation/
k6 run backend/scripts/load-tests/k6-smoke-test.js
k6 run --vus 50 --duration 2m backend/scripts/load-tests/k6-load-test.js
```

#### Demo Script
```bash
./backend/scripts/demo-script.sh
```

## 📊 Monitoring

### Health Check
```bash
curl http://your-domain/api/health
```

### Logs
```bash
# Application logs
docker logs campus-marketplace-api --tail 100 -f

# Nginx logs
tail -f /var/log/nginx/campus-marketplace-access.log
```

## 🔧 Maintenance

### Reset Demo Data
```bash
./backend/scripts/reset-demo.sh
```

### Update Application
```bash
git pull
cd backend
./scripts/deploy.sh
```

## Support

For questions or issues, please contact the development team or create an issue in the repository.

## 📚 Additional Resources

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Postman Collection**: Import `Campus Marketplace API.postman_collection.json`
- **Authentication Guide**: [AUTHENTICATION.md](backend/AUTHENTICATION.md)

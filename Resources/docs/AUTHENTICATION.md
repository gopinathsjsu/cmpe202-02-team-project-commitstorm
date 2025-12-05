# Authentication System Documentation

## Overview

The Campus Marketplace application implements **JWT (JSON Web Token) based authentication** using Spring Security. This document explains how the authentication system works, its components, and how to use it.

## Architecture

The authentication system consists of the following key components:

- **JwtUtil** - Handles token creation, validation, and extraction
- **SecurityConfig** - Configures Spring Security with JWT filter
- **JwtAuthenticationFilter** - Intercepts requests and validates tokens
- **AuthController** - Exposes authentication endpoints
- **AuthService** - Handles user registration and authentication logic
- **UserDetailsServiceImpl** - Loads user details for Spring Security

## Authentication Flow

### 1. User Registration
```
POST /api/auth/register
├── Validates email uniqueness
├── Encrypts password using BCrypt
├── Creates user with USER role and ACTIVE status
├── Generates JWT token
└── Returns AuthResponse with token and user details
```

### 2. User Login
```
POST /api/auth/login
├── Validates email/password against database
├── Checks user status (must be ACTIVE)
├── Generates JWT token
└── Returns AuthResponse with token and user details
```

### 3. Protected Resource Access
```
Request with Authorization: Bearer <token>
├── JwtAuthenticationFilter intercepts request
├── Extracts token from Authorization header
├── Validates token signature and expiration
├── Loads user details from database
├── Sets authentication in SecurityContext
└── Allows access to protected endpoints
```

## Configuration

### JWT Settings (application.yml)
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000 # 24 hours in milliseconds
```

### Security Configuration
- **CORS**: Enabled for all origins with credentials support
- **CSRF**: Disabled (not needed for JWT)
- **Session Management**: Stateless (no server-side sessions)
- **Public Endpoints**: 
  - `/api/auth/**` - Authentication endpoints
  - `/health` - Health check
  - `/swagger-ui/**`, `/api-docs/**` - API documentation

## API Endpoints

| Endpoint | Method | Description | Authentication Required |
|----------|--------|-------------|----------------------|
| `/api/auth/register` | POST | Register new user | No |
| `/api/auth/login` | POST | User login | No |
| `/api/auth/logout` | POST | Logout (client-side) | No |
| `/api/auth/me` | GET | Get current user info | Yes (JWT) |

## Request/Response Examples

### Register User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": "uuid-here",
  "name": "John Doe",
  "email": "john@example.com",
  "role": "USER",
  "status": "ACTIVE"
}
```

### Login User
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": "uuid-here",
  "name": "John Doe",
  "email": "john@example.com",
  "role": "USER",
  "status": "ACTIVE"
}
```

### Access Protected Resource
```bash
GET /api/listings
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## User Roles and Status

### Roles
- **USER**: Regular marketplace user
- **ADMIN**: Administrative user with elevated privileges

### Status
- **ACTIVE**: User can authenticate and use the system
- **SUSPENDED**: User account is temporarily disabled
- **BANNED**: User account is permanently disabled

## Security Features

1. **Token Validation**: Every request validates token signature and expiration
2. **User Status Check**: Only active users can authenticate
3. **Password Encryption**: BCrypt hashing with salt
4. **CORS Protection**: Configured for cross-origin requests
5. **Stateless Design**: No server-side session storage
6. **Role-Based Access**: User roles are embedded in Spring Security context

## JWT Token Structure

The JWT token contains three parts:

### Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "user@example.com",
  "iat": 1640995200,
  "exp": 1641081600
}
```

### Signature
HMAC-SHA256 signature using the configured secret key

## Frontend Integration

### Storing the Token
```javascript
// After successful login
const authResponse = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const data = await authResponse.json();
localStorage.setItem('token', data.token);
```

### Using the Token
```javascript
// For protected requests
const token = localStorage.getItem('token');
const response = await fetch('/api/listings', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### Handling Token Expiration
```javascript
// Check for 401 responses
if (response.status === 401) {
  localStorage.removeItem('token');
  // Redirect to login page
  window.location.href = '/login';
}
```

## Error Handling

### Common Error Responses

| Status Code | Description | Example Response |
|-------------|-------------|------------------|
| 400 | Bad Request | `{"error": "Email is already in use!"}` |
| 401 | Unauthorized | `{"error": "Invalid token"}` |
| 403 | Forbidden | `{"error": "Access denied"}` |
| 500 | Internal Server Error | `{"error": "Internal server error"}` |

## Development Notes

### Testing Authentication
1. Use the `/api/auth/register` endpoint to create test users
2. Use the `/api/auth/login` endpoint to get JWT tokens
3. Include the token in the `Authorization` header for protected endpoints
4. Use the `/api/auth/me` endpoint to verify token validity

### Token Expiration
- Tokens expire after 24 hours by default
- Clients should handle token refresh or re-authentication
- Expired tokens will result in 401 Unauthorized responses

### Security Considerations
- Never expose the JWT secret in client-side code
- Use HTTPS in production to protect tokens in transit
- Implement proper logout by removing tokens from client storage
- Consider implementing refresh tokens for better user experience

## Troubleshooting

### Common Issues

1. **"Invalid token" error**
   - Check if token is properly formatted in Authorization header
   - Verify token hasn't expired
   - Ensure secret key matches between token creation and validation

2. **"User not found" error**
   - Verify user exists in database
   - Check if user status is ACTIVE
   - Ensure email is correct

3. **CORS errors**
   - Verify CORS configuration in SecurityConfig
   - Check if frontend origin is allowed
   - Ensure credentials are properly configured

4. **Password validation fails**
   - Verify password is correctly hashed during registration
   - Check if password matches during login
   - Ensure BCrypt encoder is properly configured

## File Structure

```
backend/src/main/java/com/campus/marketplace/
├── config/
│   ├── SecurityConfig.java          # Spring Security configuration
│   └── JwtAuthenticationFilter.java # JWT request filter
├── controller/
│   └── AuthController.java          # Authentication endpoints
├── service/
│   ├── AuthService.java             # Authentication business logic
│   └── UserDetailsServiceImpl.java  # User details service
├── util/
│   └── JwtUtil.java                 # JWT utility functions
├── entity/
│   └── User.java                    # User entity with roles/status
└── dto/
    ├── AuthResponse.java            # Authentication response DTO
    ├── LoginRequest.java            # Login request DTO
    └── RegisterRequest.java         # Registration request DTO
```

## Dependencies

The authentication system requires the following Maven dependencies:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

---

*This documentation covers the complete authentication system implementation. For additional questions or clarifications, refer to the source code or contact the development team.*

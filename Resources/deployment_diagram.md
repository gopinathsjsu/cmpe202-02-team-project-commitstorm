# Campus Marketplace - Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              User Layer                                     │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                          Load Balancer (AWS ALB)                        ││
│  │                    Distributes HTTPS traffic (443)                      ││
│  └─────────────────────────────────┬───────────────────────────────────────┘│
└───────────────────────────────────┼─────────────────────────────────────────┘
                                    │ HTTPS (443)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Application Layer                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                    Spring Boot API Container                            ││
│  │                    Docker (eclipse-temurin:17-jre)                      ││
│  │                    Port: 8080 | JVM: 256m-512m                          ││
│  │                    Health Check: /api/health                            ││
│  └─────────────────────────────────┬───────────────────────────────────────┘│
└───────────────────────────────────┼─────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
┌───────────────────┬───────────────┬───────────────┬─────────────────────────┐
│         Data Layer              External Services │                         |
│  ┌────────────────┴────────────┐ ┌─────────────┴─────────────────┐          │
│  │      AWS RDS MySQL          │ │        AWS S3 Storage         │          │
│  │   campusMarket Database     │ │   campusmarketplace202        │          │
│  │   us-west-2, MySQL 8.0      │ │   Image storage, presigned    │          │
│  │   Flyway migrations         │ │   URLs (15min expiry)         │          │
│  └─────────────────────────────┘ └───────────────────────────────┘          │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────────┐│
│  │                      OpenAI API Integration                             │|
│  │                 Chatbot search functionality                            │|
│  └─────────────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
```

## Key Components:

### Application Layer
- **Spring Boot 3.3.2** REST API
- **Java 17** runtime environment
- **Docker containerized** deployment
- **JWT authentication** for security
- **Swagger/OpenAPI** documentation at `/swagger-ui.html`

### Data Layer
- **AWS RDS MySQL**: Primary database for all application data
- **AWS S3**: Image storage with presigned URL uploads
- **Flyway**: Database schema migrations

### External Integrations
- **OpenAI API**: Powers chatbot search functionality
- **AWS Services**: S3, RDS, IAM for authentication

### Networking & Security
- **Load Balancer**: Distributes traffic across instances
- **Health Checks**: Application monitoring via `/api/health`
- **HTTPS**: Secure communication (443)
- **JWT**: Token-based authentication
- **Role-based Access**: USER/ADMIN roles with Spring Security

### Configuration
- **Environment Variables**: Database credentials, API keys
- **Docker Compose**: Local development orchestration
- **Maven Build**: JAR packaging and dependency management

### Deployment Options
1. **Docker Container**: `sonalilonkar/campus-api:demo`
2. **Local Development**: Via `start.sh` script
3. **AWS Deployment**: RDS + S3 + EC2/Lambda
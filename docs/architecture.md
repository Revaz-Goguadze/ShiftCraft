# ShiftCraft Architecture

## Overview

ShiftCraft is a monolithic Spring Boot application designed for staff scheduling and time management in small healthcare facilities.

## High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │───▶│  Spring Boot    │───▶│   PostgreSQL    │
│   (Frontend)    │    │   Application   │    │   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.4.8
- **Database**: PostgreSQL 16
- **Security**: Spring Security + JWT
- **Migration**: Flyway
- **Testing**: JUnit 5, Testcontainers
- **Build**: Maven

## Module Structure

```
backend/
├── src/main/java/com/example/shiftcraft/
│   ├── api/          # REST controllers
│   ├── core/         # Domain logic and business rules
│   ├── persistence/  # JPA entities and repositories
│   ├── security/     # Authentication and authorization
│   └── web/          # Web configuration
└── src/main/resources/
    ├── db/migration/ # Flyway SQL migrations
    └── application.properties
```

## Core Components

### 1. API Layer (`api/`)
- REST controllers for external communication
- Request/response DTOs
- Input validation
- Error handling

### 2. Core Domain (`core/`)
- Business logic and rules
- Domain services
- Use case implementations
- Domain models

### 3. Persistence Layer (`persistence/`)
- JPA entities
- Repository interfaces
- Data access logic
- Query implementations

### 4. Security Layer (`security/`)
- JWT authentication
- Role-based authorization
- Security filters
- User details service

### 5. Web Configuration (`web/`)
- Spring configuration
- CORS settings
- Exception handlers
- Swagger/OpenAPI setup

## Database Design

The database follows a normalized relational model with:

- **Strong Entities**: User, Role, Skill, Location, ShiftTemplate, etc.
- **Weak Entities**: ShiftInstance (depends on ShiftTemplate + date)
- **Associative Entities**: UserRole, UserSkill, Assignment, etc.

See `docs/stage3-database-design.md` for detailed ER model.

## Security Model

### Authentication
- JWT-based stateless authentication
- Token expiration and refresh
- Password hashing with BCrypt

### Authorization
- Role-based access control (RBAC)
- Four main roles: ADMIN, MANAGER, STAFF, FINANCE
- Method-level security annotations

### API Security
- All endpoints require authentication except login/register
- Role-specific access controls
- Input validation and sanitization

## Business Rules

### Scheduling Rules
- Minimum rest hours between shifts (configurable)
- Maximum weekly hours per staff
- Skill requirements per shift template
- Location constraints

### Swap Workflow
1. Staff initiates swap request
2. Target staff accepts/declines
3. Manager provides final approval
4. System validates business rules
5. Audit trail recorded

### Leave Management
- Leave requests with approval workflow
- Automatic conflict detection with published shifts
- Accrual rules and balances

## Quality Attributes

### Performance
- Database indexing for common queries
- Pagination for large data sets
- Caching for reference data

### Reliability
- Transaction management
- Data consistency checks
- Comprehensive error handling

### Security
- Secure authentication and authorization
- Input validation
- Audit logging

### Maintainability
- Clean architecture principles
- Comprehensive testing
- Documentation
- Code standards

## Deployment

### Local Development
```bash
docker compose -f ops/docker-compose.yml up -d postgres
./mvnw -f backend/pom.xml spring-boot:run
```

### Production Considerations
- Environment-specific configuration
- Database connection pooling
- Logging configuration
- Health monitoring
- Backup strategies
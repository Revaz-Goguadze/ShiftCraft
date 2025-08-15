# ShiftCraft

Staff scheduling and time management for small clinics and labs

## Why

Prevent illegal rosters and make swaps auditable

## Stack

Java 21, Spring Boot 3, PostgreSQL, Flyway, JUnit 5, Testcontainers

## Run locally

```bash
docker compose up -d postgres
./mvnw -q -f backend/pom.xml -DskipTests package
java -jar backend/target/shiftcraft-0.0.1-SNAPSHOT.jar
```

## Configuration

Set environment variables:

```bash
DB_URL=jdbc:postgresql://localhost:5432/shiftcraft
DB_USER=shift
DB_PASS=shift
JWT_SECRET=replace-me
```

## Modules

- `backend/` - Spring Boot application
  - `api/` - REST controllers
  - `core/` - Domain logic
  - `persistence/` - JPA entities and repositories
  - `security/` - Authentication and authorization
  - `web/` - Web configuration

## Features

- Users and roles management
- Shift template builder
- Calendar with assign, swap request, approve flow
- Leave management with accrual rules
- Timesheet export CSV
- Audit log on approvals

## API Endpoints

- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- CRUD for skills and roles
- CRUD for shift templates
- `POST /api/v1/shifts:publish` - Publish schedule
- `POST /api/v1/swaps:request` - Request swap
- `POST /api/v1/leaves:request` - Request leave
- `GET /api/v1/timesheets:period` - Get timesheets
- `GET /api/v1/audit` - Audit events

## Development

### Prerequisites

- Java 21
- Docker and Docker Compose
- Maven 3.6+

### Setup

1. Start PostgreSQL:
   ```bash
   docker compose up -d postgres
   ```

2. Run tests:
   ```bash
   ./mvnw -f backend/pom.xml test
   ```

3. Start application:
   ```bash
   ./mvnw -f backend/pom.xml spring-boot:run
   ```

## Docs

See `docs/` for detailed documentation:
- `docs/stage1-capstone-overview.md` - Project overview and setup
- `docs/stage2-use-cases.md` - Use cases and requirements
- `docs/stage3-database-design.md` - Database schema and ER model
- `docs/architecture.md` - System architecture

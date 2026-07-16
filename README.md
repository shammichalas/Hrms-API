# HRMS API

A modular, production-style Human Resource Management System backend built with **Spring Boot 3** and **Java**. It models the core workflows of an HR platform — employee lifecycle, departments, attendance, leave, payroll, notifications, and dashboard analytics — behind a secured, versioned REST API.

## Why this project

Built to demonstrate real-world Spring Boot backend patterns: layered architecture, JWT-based auth, JPA with specifications for dynamic filtering, DTO mapping via MapStruct, centralized exception handling, and OpenAPI documentation — not just CRUD boilerplate.

## Tech stack

- **Java + Spring Boot** — Web, Data JPA, Security, Validation
- **PostgreSQL** — primary datastore
- **Flyway** — versioned database migrations
- **Spring Security + JWT (jjwt)** — stateless authentication with access/refresh tokens
- **MapStruct** — entity ↔ DTO mapping
- **Lombok** — boilerplate reduction
- **springdoc-openapi** — Swagger UI at `/swagger-ui.html`
- **Docker / docker-compose** — containerized local setup

## Architecture

Feature-based package structure — each domain owns its controller, DTOs, entity, mapper, repository, and service:

```
com.company.hrms
├── auth          # register / login / refresh (JWT issuance)
├── employee      # employee CRUD, search & filtering, soft delete
├── department    # department management
├── attendance    # check-in/out and attendance records
├── leave         # leave requests and approvals
├── payroll       # payroll records and payment status
├── notification  # in-app notifications
├── dashboard     # aggregated stats for the dashboard view
├── analytics     # reporting/analytics endpoints
├── settings      # system/org settings
├── security      # JWT filter, JwtService, UserDetailsService
└── common        # ApiResponse envelope, BaseEntity, global exception handling
```

Cross-cutting design choices:
- **`ApiResponse<T>`** wraps every response (`success`, `message`, `data`, `timestamp`) for a consistent contract.
- **`BaseEntity`** gives every entity auditing fields (`createdAt`/`updatedAt`/`createdBy`/`updatedBy`) and soft-delete (`isDeleted`) via Spring Data JPA auditing.
- **`@SQLDelete` / `@SQLRestriction`** on `Employee` implement soft deletes transparently — deleted rows are excluded from every query without extra `WHERE` clauses in application code.
- **`Specification`-based dynamic search** on the employee listing endpoint (filter by name/email/job title, department, role, status — combinable).
- **Role-based access control** via `@PreAuthorize` (`ADMIN`, `HR`, `EMPLOYEE`) enforced at the controller layer.
- **`GlobalExceptionHandler`** maps domain exceptions (`ResourceNotFoundException`, `DuplicateResourceException`, `UnauthorizedException`, validation errors) to proper HTTP status codes and a uniform error body.

## API overview

| Module | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `/login`, `/refresh` |
| Employees | `GET/POST /api/employees`, `GET/PUT/DELETE /api/employees/{id}` (paginated, filterable, RBAC-protected) |
| Departments | CRUD under `/api/departments` |
| Attendance | check-in/out and history under `/api/attendance` |
| Leave | request/approve/reject under `/api/leave` |
| Payroll | payroll records under `/api/payroll` |
| Notifications | `/api/notifications` |
| Dashboard / Analytics | aggregated stats under `/api/dashboard`, `/api/analytics` |

Full interactive documentation is available via Swagger UI once the app is running.

## Running locally

### Prerequisites
- JDK 21+
- Maven
- PostgreSQL (or Docker)

### Environment variables
Copy `.env.example` to `.env` and fill in:
```
DATABASE_URL=jdbc:postgresql://localhost:5432/hrms
DATABASE_USERNAME=...
DATABASE_PASSWORD=...
JWT_SECRET=...
```

### With Docker Compose
```bash
docker-compose up --build
```

### Locally with Maven
```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

### Running tests
```bash
mvn test
```

## Companion project

The frontend for this API lives in [Hrms-UI](https://github.com/shammichalas/Hrms-UI) (React + TypeScript + Vite).

# Resource Booking System API

A RESTful Resource Booking System built with Spring Boot, Java, and Spring Security. This API allows users to view available resources and manage their reservations, while administrators have full Role-Based Access Control (RBAC) to manage all resources and reservations.

## Tech Stack

| Component         | Technology                          |
|-------------------|--------------------------------------|
| **Language**      | Java 17+                             |
| **Framework**     | Spring Boot 3.3.x                    |
| **Security**      | Spring Security 6, JWT (JSON Web Tokens) |
| **Database**      | MySQL                                |
| **ORM**           | Spring Data JPA / Hibernate          |
| **Documentation** | OpenAPI 3 / Swagger UI               |

## Prerequisites

- Java 17 or higher installed
- Maven installed
- MySQL server running locally (default port `3306`)

## Database Setup & Environment Variables

Before running the application, create a database in MySQL:

```sql
CREATE DATABASE booking_db;
```

The application relies on the following environment variables. You can set these in your OS environment or IDE configuration before starting the application:

| Variable         | Description                              | Default Value                    |
|-------------------|-------------------------------------------|-----------------------------------|
| `DB_HOST`         | MySQL Server Host                         | `localhost`                       |
| `DB_PORT`         | MySQL Server Port                         | `3306`                            |
| `DB_NAME`         | Database Name                             | `booking_db`                      |
| `DB_USER`         | MySQL Username                            | `root`                            |
| `DB_PASSWORD`     | MySQL Password                            | `root`                            |
| `JWT_SECRET`      | 256-bit Base64 encoded secret for JWT     | *(Provided in `application.yml`)* |
| `JWT_EXPIRATION`  | JWT expiration time in milliseconds       | `86400000` (24 Hours)             |

> **Note:** If these variables are not explicitly set in the environment, the application will safely fall back to the default values defined in `application.yml`.

## Running the Application

1. Clone or extract the project repository.
2. Open a terminal in the root directory (where `pom.xml` is located).
3. Build the project using Maven:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on port `8081`. Upon the first startup, Hibernate will automatically generate all necessary database tables (`users`, `resources`, `reservations`).

## Seed Users

A `CommandLineRunner` is configured to automatically populate the database with two default users upon the first startup for immediate testing:

**Admin Account** (Full CRUD Access)
- Email: `admin@system.local`
- Password: `admin123`

**User Account** (Read-Only & Own Reservations)
- Email: `user@system.local`
- Password: `user123`

## API Documentation (Swagger UI)

Once the application is running, comprehensive interactive API documentation is available via Swagger UI.

**URL:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

**How to authenticate in Swagger:**

1. Execute `POST /auth/login` with one of the seed user credentials.
2. Copy the returned token string (without quotes).
3. Click the green **Authorize** button at the top of the page and paste the token.

## Key Features Implemented

- **Stateless Authentication** — JWT-based login and token validation.
- **Role-Based Access Control (RBAC)** — Strict enforcement distinguishing `ADMIN` and `USER` privileges.
- **Data Isolation** — Users can only retrieve and cancel their own reservations; Admins bypass this filter.
- **Dynamic Queries** — JPA Specifications implemented for filtering reservations by status and price bounds.
- **Pagination & Sorting** — Efficient data retrieval handling `page`, `size`, `sortBy`, and `sortDir` parameters.
- **Global Error Handling** — Clean, standardized JSON responses for validation failures (`@Valid`), unauthorized access, and business logic exceptions.
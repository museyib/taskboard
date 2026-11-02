# Taskboard

Taskboard is a REST API for managing personal tasks and labels. It provides user registration and JWT-based authentication, task organization and search, and CSV export of completed work.

## Features

- Register and authenticate users; refresh and revoke refresh tokens.
- Protect API routes with JWT access tokens. Failed logins can temporarily lock an account.
- Create, view, update, and soft-delete tasks belonging to the authenticated user.
- Filter and paginate tasks by status, deadline, keyword, and label; sort by a task field.
- Organize tasks with labels, including label creation, editing, listing, and soft deletion.
- Export a user's completed tasks as CSV.
- Run a scheduled job each day at midnight to record notifications for expired tasks.
- Generate API documentation with Springdoc OpenAPI and Swagger UI.

## Technology

- Java 25 and Spring Boot 4.1.2
- Spring MVC, Spring Security, Spring Data JPA, and Bean Validation
- PostgreSQL, with schema migrations managed by Flyway
- Maven (Maven Wrapper included)
- H2 for tests

## Requirements

- JDK 25
- PostgreSQL
- No separate Maven installation is needed when using the included wrapper

## Getting started

1. Create a PostgreSQL database named `taskboard` (or configure a different database URL).
2. Configure the database and JWT settings using environment variables or a `.env` file in the project root. The application imports `.env` as optional Java properties.
3. Start the application:

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows, use `mvnw.cmd spring-boot:run`.

Flyway applies the schema migration at startup. Hibernate schema generation is disabled, so the database must be reachable when the application starts.

### Configuration

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/taskboard` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET_KEY` | `your-secret-key` | Secret used to sign JWTs; configure a secure secret outside local development |
| `ACCESS_TOKEN_EXPIRATION` | `3600000` | Access-token lifetime in milliseconds (1 hour) |
| `REFRESH_TOKEN_EXPIRATION` | `604800000` | Refresh-token lifetime in milliseconds (7 days) |

The defaults are for local development. In particular, replace the JWT secret and database credentials before using the service in a shared or production environment.

## API overview

The base URL when running locally is `http://localhost:8080`.

### Authentication

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register an account |
| `POST` | `/api/auth/login` | Authenticate and receive tokens |
| `POST` | `/api/auth/refresh` | Exchange a refresh token for a new token pair |
| `POST` | `/api/auth/logout` | Revoke a refresh token |

Registration, login, refresh, and logout are public routes. Send the access token as a bearer token for protected requests:

```http
Authorization: Bearer <access-token>
```

### Tasks

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/tasks?page=0&size=20` | List the signed-in user's tasks |
| `POST` | `/api/tasks` | Create a task |
| `GET` | `/api/tasks/{id}` | Get a task and its labels |
| `PUT` | `/api/tasks/{id}` | Update a task |
| `DELETE` | `/api/tasks/{id}` | Soft-delete a task |
| `GET` | `/api/tasks/export` | Export completed tasks as CSV |

The task list requires `page` and `size`. Optional filters are `status`, `deadLine`, `keyword`, and `label`. Use `sort=field,asc` or `sort=field,desc` to choose a sort field and direction; the default is deadline descending. A task status cannot be set to `BLOCKED` through the update endpoint. Task statuses are defined by the API's `TaskStatus` enum.

### Labels

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/labels` | List active labels |
| `POST` | `/api/labels` | Create a label |
| `GET` | `/api/labels/{id}` | Get a label |
| `PUT` | `/api/labels/{id}` | Update a label |
| `DELETE` | `/api/labels/{id}` | Soft-delete a label |

For interactive documentation, open `http://localhost:8080/swagger-ui/index.html`. The OpenAPI document is available at `/v3/api-docs`.

## Typical workflow

1. Register an account and log in.
2. Keep the returned access token and send it as a bearer token for task requests. Store the refresh token securely for renewal; call logout when ending a session.
3. Create labels, then create tasks and organize them with labels.
4. Query tasks with pagination, filters, and sorting as the task list grows.
5. Mark tasks as `DONE` when completed and download the CSV export when needed.

## Development

Run the test suite with:

```bash
./mvnw test
```

Tests use H2 and the test configuration in `src/test/resources/application.yaml`; a PostgreSQL instance is not required for the test suite. The production application uses PostgreSQL and applies migrations from `src/main/resources/db/migration`.

## Project layout

```text
src/main/java/llc/devlab/app/
  controller/       Task and label REST endpoints
  security/         Authentication, JWT, and security configuration
  service/          Application services
  repository/       Data access
  model/            Entities, DTOs, and task status
  mapper/           Entity/DTO and CSV mapping
src/main/resources/
  application.yaml  Runtime configuration
  db/migration/     Flyway schema migrations
src/test/           Integration and application tests
```

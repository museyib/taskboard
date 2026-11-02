# Taskboard

Taskboard is a REST API for organizing work into tasks and labels. It provides account authentication, task and label management, and a CSV export of completed tasks.

## Features

- Register and authenticate users with email and password.
- Issue JWT access and refresh tokens, refresh access, and log out by invalidating a refresh token.
- Protect application routes with JWT authentication; task queries are scoped to the authenticated user.
- Create, view, update, and delete tasks, including descriptions, deadlines, labels, and status.
- Organize tasks with labels.
- Export completed tasks as CSV.
- Track failed logins and temporarily lock accounts after repeated failures.
- Use Flyway migrations to initialize the PostgreSQL schema. The schema includes a task-notification table; no notifications API route is currently exposed.
- Browse the API through the generated OpenAPI/Swagger UI.

## Technology

- Java 25
- Spring Boot 4.1.2, Spring MVC, Spring Security, and Spring Data JPA
- PostgreSQL and Flyway
- JWT (`jjwt`)
- MapStruct
- Springdoc OpenAPI / Swagger UI
- Maven Wrapper

## Workflow

1. Start PostgreSQL and create a database named `taskboard` (or configure another database URL).
2. Register a user with `POST /api/auth/register`.
3. Sign in with `POST /api/auth/login`; the response includes an access token and a refresh token.
4. Send the access token as a Bearer token when calling protected task and label endpoints.
5. Create labels and tasks, then list or update tasks as work progresses. Task statuses are `TODO`, `IN_PROGRESS`, `BLOCKED`, and `DONE`.
6. Refresh access using `POST /api/auth/refresh` as needed, and use `POST /api/auth/logout` to invalidate a refresh token.
7. Download the completed-task CSV from `GET /api/tasks/export`.

## API overview

Authentication endpoints are public. All other routes require authentication unless otherwise configured.

| Method | Route | Description |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Authenticate and receive tokens |
| `POST` | `/api/auth/refresh` | Refresh tokens |
| `POST` | `/api/auth/logout` | Invalidate a refresh token |
| `GET` | `/api/tasks` | List the authenticated user's tasks |
| `GET` | `/api/tasks/{id}` | Get a task and its labels |
| `POST` | `/api/tasks` | Create a task |
| `PUT` | `/api/tasks/{id}` | Update a task, including its status |
| `DELETE` | `/api/tasks/{id}` | Delete a task |
| `GET` | `/api/tasks/export` | Export completed tasks as CSV |
| `GET` | `/api/labels` | List labels |
| `GET` | `/api/labels/{id}` | Get a label |
| `POST` | `/api/labels` | Create a label |
| `PUT` | `/api/labels/{id}` | Update a label |
| `DELETE` | `/api/labels/{id}` | Delete a label |

The `BLOCKED` status is present in the status enum, but the current task update service rejects requests that set a task to `BLOCKED`.

## Requirements

- JDK 25
- PostgreSQL

## Configuration

The application reads these environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/taskboard` | JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET_KEY` | `your-secret-key` | JWT signing key |
| `ACCESS_TOKEN_EXPIRATION` | `3600000` | Access-token lifetime in milliseconds (1 hour) |
| `REFRESH_TOKEN_EXPIRATION` | `604800000` | Refresh-token lifetime in milliseconds (7 days) |

For a local run, set credentials and a strong, private JWT signing key in your environment before starting the app. The checked-in defaults are development conveniences and should not be used for a deployed environment. Never commit real credentials or signing keys.

For example, configure the database and key in your shell (replace the example values with your local settings):

```sh
export DB_URL='jdbc:postgresql://localhost:5432/taskboard'
export DB_USERNAME='postgres'
export DB_PASSWORD='change-me'
export JWT_SECRET_KEY='replace-with-a-private-signing-key'
```

Flyway applies migrations from `src/main/resources/db/migration` at application startup. Hibernate schema generation is disabled, so the database must be available and migrations must succeed before the API can operate.

## Run

From the project root, with PostgreSQL running and the environment configured:

```sh
./mvnw spring-boot:run
```

The application uses Spring Boot's configured default HTTP port (`8080`). Open Swagger UI at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html), or access the OpenAPI document at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

Run the test suite with:

```sh
./mvnw test
```

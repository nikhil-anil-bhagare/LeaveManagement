# Leave Management API

A Spring Boot REST API for an organization's HR system that allows employees to submit and
manage leave requests. Built with a clean, layered architecture (Controller → Service →
Repository) and an in-memory data store.

## Project Overview

- **Framework:** Spring Boot 4.1.1 (Spring Framework 7), Java 21
- **Build tool:** Gradle
- **Storage:** In-memory (`ConcurrentHashMap`) — no database required
- **Architecture:** Controller → Service → Repository, with dedicated `dto`, `model`,
  `exception`, and `config` packages

### Package Structure

```
com.professionals.leavemanagement
├── controller     REST controllers
├── service        Business logic (interface + implementation)
├── repository     Data access (interface + in-memory implementation)
├── model           Domain entities (LeaveRequest, LeaveType, LeaveStatus)
├── dto            Request/response DTOs with Bean Validation
├── exception      Custom exceptions + @RestControllerAdvice global handler
└── config         Externalized configuration (@ConfigurationProperties)
```

## Build and Run Instructions

### Prerequisites

- Java 21+
- No local database or external services required

### Build

```bash
./gradlew build
```

### Run (default profile → dev)

```bash
./gradlew bootRun
```

Or run the packaged jar:

```bash
./gradlew bootJar
java -jar build/libs/LeaveManagement-0.0.1-SNAPSHOT.jar
```

The application starts on `http://localhost:8080`.

### Run with an explicit profile

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

A `dev` Spring profile is provided (`application-dev.yml`) with `DEBUG` logging for the
application package; `application.yml` holds shared defaults and activates `dev` by default.

## API Endpoint Summary

Base path: `/leaves`

| Method | Endpoint       | Description                     | Success Status |
|--------|----------------|----------------------------------|-----------------|
| GET    | `/leaves`      | Retrieve all leave requests      | 200 OK          |
| GET    | `/leaves/{id}` | Retrieve a leave request by ID   | 200 OK          |
| POST   | `/leaves`      | Create a new leave request       | 201 Created     |
| PUT    | `/leaves/{id}` | Update an existing leave request | 200 OK          |
| PATCH  | `/leaves/{id}/status` | Update a leave request's status (e.g. approve/reject/cancel) | 200 OK |
| DELETE | `/leaves/{id}` | Delete a leave request           | 204 No Content  |

Every response body is wrapped in a consistent `ApiResponse` envelope, with one deliberate
exception: `DELETE` returns `204 No Content` with no body, per HTTP semantics (a `204`
response must not carry a payload).

```json
{
  "success": true,
  "message": "Leave request retrieved successfully",
  "data": { "...": "..." },
  "errors": null,
  "errorCode": null,
  "timestamp": "2026-09-29T10:15:30"
}
```

### Leave Request fields

| Field        | Type   | Notes                                                             |
|--------------|--------|--------------------------------------------------------------------|
| `id`         | Long   | Server-generated                                                   |
| `employeeId` | String | Required                                                            |
| `leaveType`  | enum   | One of `SICK`, `CASUAL`, `EARNED`, `UNPAID`, `MATERNITY`, `PATERNITY` |
| `startDate`  | date   | Required, must be today or in the future                           |
| `endDate`    | date   | Required, must not be before `startDate`                           |
| `reason`     | String | Required, max 500 characters                                       |
| `status`     | enum   | Server-managed, one of `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED` (new requests default to `PENDING`; changed only via `PATCH /leaves/{id}/status`) |

### Sample request (POST /leaves)

```json
{
  "employeeId": "EMP001",
  "leaveType": "SICK",
  "startDate": "2026-10-01",
  "endDate": "2026-10-03",
  "reason": "Not feeling well"
}
```

### Sample request (PATCH /leaves/{id}/status)

```json
{
  "status": "APPROVED"
}
```

### Error responses

Validation errors, not-found errors, and unexpected errors are all handled centrally by
`GlobalExceptionHandler` (`@RestControllerAdvice`) and returned in the same `ApiResponse`
envelope with `success: false` and a client-friendly `message` — no stack traces or
internal exception details are ever exposed to the client (unexpected errors are logged
server-side and returned as a generic message).

Every error also carries a stable `errorCode` — a fixed identifier clients can branch on
programmatically, independent of the (freely rewordable) `message` text or the HTTP status
code already present on the response itself:

| `errorCode`             | When                                             | HTTP status |
|--------------------------|---------------------------------------------------|-------------|
| `LEAVE_NOT_FOUND`         | Leave request id does not exist                   | 404         |
| `INVALID_LEAVE_REQUEST`   | End date before start date, or duration too long  | 400         |
| `VALIDATION_FAILED`       | Bean Validation failed on the request body        | 400         |
| `INVALID_PARAMETER`       | Path/query parameter has the wrong type           | 400         |
| `MALFORMED_REQUEST`       | Request body is missing or not valid JSON         | 400         |
| `INTERNAL_ERROR`          | Unexpected server-side failure                    | 500         |

```json
{
  "success": false,
  "message": "Leave request not found with id: 99",
  "data": null,
  "errors": null,
  "errorCode": "LEAVE_NOT_FOUND",
  "timestamp": "2026-09-29T10:15:30"
}
```

Bean Validation failures populate the `errors` array with one message per invalid field:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": ["employeeId is required", "reason is required"],
  "errorCode": "VALIDATION_FAILED",
  "timestamp": "2026-09-29T10:15:30"
}
```

## Testing Instructions

Run the full test suite:

```bash
./gradlew test
```

Test reports are generated at `build/reports/tests/test/index.html`.

The test suite includes:

- **Service layer unit tests** (`LeaveServiceImplTest`) — Mockito-based tests covering
  CRUD operations, not-found handling, and business validation (date range, max leave
  duration).
- **Controller tests** (`LeaveControllerTest`) — `@WebMvcTest` + `MockMvc` tests covering
  all endpoints, success paths, validation failures, and not-found scenarios.
- **Application context test** (`LeaveManagementApplicationTests`) — verifies the Spring
  context loads successfully.

## Configuration

`leave-management.max-leave-duration-days` (default `35`, set in `application.yml`) caps
the maximum number of days allowed for a single leave request, bound via
`LeaveManagementProperties` (`@ConfigurationProperties`).

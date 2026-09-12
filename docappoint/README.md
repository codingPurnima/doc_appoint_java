# DocAppoint

DocAppoint is a Spring Boot REST API for appointment scheduling between patients and doctors. It includes user authentication, JWT-based authorization, doctor registration secret validation, slot creation, and appointment booking workflows.

## Tech Stack

- Java 20
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- PostgreSQL
- Maven
- JWT (jjwt)

## Project Structure

- `src/main/java/com/docappoint/controller` - REST controllers
- `src/main/java/com/docappoint/service` - business logic
- `src/main/java/com/docappoint/entity` - JPA entities
- `src/main/java/com/docappoint/dto` - request/response DTOs
- `src/main/java/com/docappoint/repository` - repositories
- `src/main/java/com/docappoint/security` - JWT and security config
- `src/test/java` - unit/integration tests

## Prerequisites

- Java 20+
- Maven 3.9+
- PostgreSQL database

## Environment Variables

Create the following environment variables before running the application:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/docappoint
DATABASE_USERNAME=your_username
DATABASE_PASSWORD=your_password
SECRET_KEY=your-long-random-jwt-secret
DOCTOR_REGISTER_SECRET=your-doctor-registration-secret
```

For Windows PowerShell:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/docappoint"
$env:DATABASE_USERNAME="your_username"
$env:DATABASE_PASSWORD="your_password"
$env:SECRET_KEY="your-long-random-jwt-secret"
$env:DOCTOR_REGISTER_SECRET="your-doctor-registration-secret"
```

## Running the Application

From the project root (`docappoint`):

### On Linux/macOS

```bash
./mvnw spring-boot:run
```

### On Windows

```powershell
./mvnw.cmd spring-boot:run
```

The application listens on the default Spring Boot port:

```text
http://localhost:8080
```

## Database Setup

This project expects a PostgreSQL database connection configured with the `DATABASE_*` variables above.

The project uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

So Hibernate will create/update tables automatically when the app starts.

## Authentication

The API uses JWT bearer tokens.

### Registration

- `POST /register` - register a patient
- `POST /register/doctor?secret=<secret>` - register a doctor using the doctor registration secret

### Login

- `POST /login`

Example body:

```json
{
  "username": "alice",
  "password": "secret123"
}
```

### Token Refresh

- `POST /auth/refresh`

## Main API Endpoints

### User

- `GET /users/me` - get current authenticated user details

### Slots

- `POST /slots/generate` - create slots for the authenticated doctor
- `GET /slots?date=YYYY-MM-DD` - get a doctor's slots for a given date
- `GET /slots/available?date=YYYY-MM-DD` - get available slots for a date
- `PATCH /slots/{slot_id}/freeze` - freeze/unfreeze a slot

### Appointments

- `POST /appointments/book` - book an appointment
- `GET /appointments/me` - get current patient's appointments
- `GET /appointments/doctor` - get current doctor's appointments
- `PATCH /appointments/{appointment_id}/complete` - mark an appointment as complete
- `PUT /appointments/{appointment_id}/cancel` - cancel an appointment

## Example Request Flow

1. Register a patient or doctor
2. Log in with username/password
3. Receive JWT access token and refresh token
4. Use bearer token in the `Authorization` header for protected routes

Example:

```http
Authorization: Bearer <access_token>
```

## Testing

Run tests with:

```bash
./mvnw test
```

## Notes

- The app currently validates a special secret for doctor registration.
- JWT signing requires a non-empty `SECRET_KEY` value.
- If the database is unavailable or env vars are missing, the Spring Boot app will fail to start.

# Project Overview: liveklass

`liveklass` is a Spring Boot-based backend application for a live class platform. It provides functionalities for member management, instructor (creator) registration, lecture management, and course enrollment.

## Tech Stack
- **Language:** Java 17
- **Framework:** Spring Boot (WebMVC, Data JPA)
- **Database:** H2 (In-memory/Console), MySQL
- **Build Tool:** Gradle
- **Library:** Lombok, Jakarta Persistence (JPA)

## Architecture & Structure
The project follows a standard layered architecture:
- **Controller (`com.liveklass.controller`):** Handles REST API requests and uses DTOs for data transfer.
- **Service (`com.liveklass.service`):** Contains business logic and manages transactions.
- **Repository (`com.liveklass.repository`):** Spring Data JPA interfaces for database access.
- **Domain (`com.liveklass.domain`):** JPA entities representing the core business models and rules.

### Key Domains
- **Member:** Represents platform users (email, name).
- **CreatorProfile:** Additional profile for members registered as instructors.
- **Lecture:** Class details including creator, title, price, capacity, and status (`OPEN`, `CLOSED`, etc.).
- **Enrollment:** Records of members signing up for lectures with status tracking (`PENDING`, `CONFIRMED`, `CANCELLED`).

## Building and Running
- **Build:** `./gradlew build`
- **Run:** `./gradlew bootRun`
- **Test:** `./gradlew test`
- **H2 Console:** Accessible at `/h2-console` (configured via `spring-boot-h2console`).

## Development Conventions
- **Entity Design:**
    - Use `@NoArgsConstructor(access = AccessLevel.PROTECTED)` to enforce the use of constructors or builders.
    - Keep domain logic within entities when it pertains to their internal state (Rich Domain Model).
- **Transaction Management:**
    - Use `@Transactional(readOnly = true)` at the service class level.
    - Use `@Transactional` for methods that perform write operations.
- **DTOs:**
    - Request DTOs should use `final` fields to ensure immutability and `RequiredArgsConstructor` for initialization.
- **API Mapping:**
    - API endpoints are prefixed with `/api` (e.g., `/api/members`).
- **Coding Convention**
    - Mark every method parameter as final.
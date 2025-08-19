# SkillExchange Platform

SkillExchange is a web application built with Spring Boot that enables users to connect and exchange skills. It features user registration, skill search, skill exchange requests, and real-time chat. The backend uses PostgreSQL for data storage and Liquibase for database migrations.

## Features
- User registration and authentication
- Search users by skills
- Send and manage skill exchange requests
- Real-time chat between users
- OTP-based verification for secure actions

## Technologies Used
- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Liquibase
- Gradle
- WebSocket (for chat)
- Docker (optional)

## Getting Started

### Prerequisites
- Java 17+ (auto-provisioned by Gradle toolchains)
- Docker (for local Postgres)
- Optional: Local PostgreSQL if you don't use Docker

### Setup Instructions (Local with Docker Postgres)
1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd skillexchange
   ```
2. **Start PostgreSQL:**
   ```bash
   docker compose up -d postgres
   ```
   - This starts Postgres 16 with DB `skillexchange` and user/password `admin/admin` (see `docker-compose.yml`).
3. **Configure app (optional):**
   - Defaults in `src/main/resources/application.yml` point to `jdbc:postgresql://localhost:5432/skillexchange` with `admin/admin` and Liquibase enabled.
   - JPA is set to `ddl-auto=validate`. Liquibase owns the schema.
4. **Run the app:**
   ```bash
   ./gradlew bootRun
   ```
   - First run will auto-download a JDK 17 via Gradle toolchains if needed.
5. **Access the API:**
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/v3/api-docs

### Docker Setup
- To run only the database using Docker Compose (recommended): see above.
- If you containerize the app as well, add a service in `docker-compose.yml` that depends on `postgres` and exports port 8080.

## Project Structure
- `src/main/java/com/skillexchange/` - Main application code
- `src/main/resources/` - Configuration and migration scripts
- `build.gradle` - Build configuration
- `docker-compose.yml` - Container orchestration

## API Endpoints (high level)
- Auth: `/auth/**` (signup-to-otp, verifyOtp, login)
- User: per `UserApi`
- Search skills: per `SearchSkillsApi`
- Skill exchange: per `SkillExchangeApi`
- Chat: WebSocket STOMP under `/ws-chat`

See Swagger UI for the full contract.

## Database Schema
- Managed by Liquibase. Master changelog: `src/main/resources/db/changelog/changelog-master.yaml`.
- Initial DDL: `src/main/resources/db/changelog/scripts/2025-08-15-001-init-schema.sql`.
- JPA validation: `spring.jpa.hibernate.ddl-auto=validate` prevents schema drift.

### Postgres specifics
- `users.skills` is a `TEXT[]` array (indexed via GIN). Queries use `ANY(skills)`.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the MIT License.

## Security
- Stateless Spring Security with a JWT filter authenticates requests:
  - `Authorization: Bearer <token>` header is parsed by a `OncePerRequestFilter`.
  - On success, the user is set in `SecurityContext` and protected endpoints require authentication.
- You can inject the authenticated principal via Spring Security if needed.

## Contact
For questions or support, contact the maintainer at [as861949578@gmail.com].


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
- Java 17 or higher
- PostgreSQL
- Gradle
- Docker (optional)

### Setup Instructions
1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd skillexchange
   ```
2. **Configure the database:**
   - Edit `src/main/resources/application.yml` with your PostgreSQL credentials.
   - Ensure the database `skillexchange` exists, or create it.
3. **Run database migrations:**
   - Liquibase will apply migrations automatically on application startup.
   - Alternatively, run SQL scripts in `src/main/resources/db/changelog/scripts/` manually if needed.
4. **Build and run the application:**
   ```bash
   ./gradlew build
   ./gradlew bootRun
   ```
5. **Access the application:**
   - The API will be available at `http://localhost:8080/`

### Docker Setup
- To run the application and database using Docker Compose:
  ```bash
  docker-compose up --build
  ```

## Project Structure
- `src/main/java/com/skillexchange/` - Main application code
- `src/main/resources/` - Configuration and migration scripts
- `build.gradle` - Build configuration
- `docker-compose.yml` - Container orchestration

## API Endpoints
- User management: `/api/user/*`
- Skill search: `/api/search-skills/*`
- Skill exchange: `/api/skill-exchange/*`
- Chat: `/api/chat/*`

## Database Schema
- See `src/main/resources/db/changelog/scripts/2025-08-15-001-init-schema.sql` for table definitions.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
This project is licensed under the MIT License.

## Contact
For questions or support, contact the maintainer at [as861949578@gmail.com].


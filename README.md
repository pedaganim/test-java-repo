# java-springboot-template
Creating sample template for java projects

## Overview
Starter template for future Spring Boot projects using:
- **Backend**: Spring Boot, Gradle, H2 (in-memory), JUnit 5
- **Frontend**: Vite, React, TypeScript (dev proxy to backend)

## Project Structure
- **build.gradle** / **settings.gradle**: Gradle config
- **src/main/java**: Spring Boot application and controllers
- **src/main/resources/application.properties**: H2 + server config
- **src/test/java**: JUnit tests
- **ui/**: Vite React TS app (Hello World, proxies `/api` to backend)

## Prerequisites
- Java 25 toolchain (project is configured for JDK 25)
  - Gradle will auto-download the required JDK if not installed locally (see `gradle.properties`).
  - Optional local install (macOS): `brew install --cask temurin`
- Gradle 8+ (wrapper included)
- Node.js 18+ and npm

## Backend: Run & Test
```bash
# from repo root
gradle bootRun            # run backend on http://localhost:8080
gradle test               # run tests

# optional: re-generate Gradle wrapper (then use ./gradlew)
gradle wrapper
```

## Frontend (UI): Run
```bash
cd ui
npm install
npm run dev               # http://localhost:5173 (proxies /api to :8080)
```

The UI calls `GET /api/hello` which returns "Hello, World!" from the backend.

## H2 Console
- Enabled at `/h2-console` (backend must be running)
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`, Password: empty

## Notes
- Vite dev server proxies `/api` to `http://localhost:8080`. Adjust `ui/vite.config.ts` if needed.
- Modify `spring.jpa.hibernate.ddl-auto` if you want persistence beyond in-memory defaults.

## Swagger / OpenAPI
- Added via `springdoc-openapi`.
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Spring Profiles & Config
- Profiles are defined in `application.yml`:
  - **dev** (default): H2 in-memory, console at `/h2-console`
  - **test**: H2, `create-drop`, random port
  - **pg**: PostgreSQL (use env vars to point to DB)
- Switch profile:
  - Env var: `SPRING_PROFILES_ACTIVE=pg ./gradlew bootRun`
  - Or in Docker: `SPRING_PROFILES_ACTIVE=pg`

## Docker
Build API image:
```bash
docker build -t java-springboot-template-api .
```
Run API (dev profile, H2):
```bash
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev java-springboot-template-api
```

UI image (Nginx, static):
```bash
docker build -t java-springboot-template-ui ./ui
docker run --rm -p 8081:80 java-springboot-template-ui
```

## Docker Compose (API + UI + Postgres)
```bash
# dev (H2): db still runs but API uses dev unless you set pg
docker compose up --build

# pg profile (API will use Postgres service)
SPRING_PROFILES_ACTIVE=pg docker compose up --build
```
Services:
- API: http://localhost:8080
- UI: http://localhost:8081 (Nginx, proxies `/api` to `api:8080`)
- Postgres: localhost:5432 (user/pass/db `app`)

## Next Steps (customize for new projects)
- Update `group` and package names under `src/main/java`
- Add new REST controllers and services
- Add database schema/migrations if moving off in-memory H2
- Extend UI with routes/components and shared types

# Repository Guidelines

## Product Spec Source
- Refer to `PRD.md` for product specification (scope, purpose, user-visible behavior, and business rules).
- Use this file (`AGENTS.md`) for implementation guidance only (architecture, coding conventions, testing workflow, and delivery process).
- If there is a conflict, align implementation decisions with `PRD.md` first, then update this file as needed.

## Project Structure & Module Organization
- Core backend code lives in `src/main/java/edu/handong/csee/histudy`.
- Package layout follows Spring conventions: `controller`, `service`, `repository`, `domain`, `dto`, `config`, and `exception`.
- Runtime config and assets are in `src/main/resources` (`application.yml`, `logback/`, `sql/`, `images/`, `templates/`).
- Tests live in `src/test/java/edu/handong/csee/histudy`, with shared fixtures/utilities under `support` and fake repositories under `service/repository/fake`.
- API contract is maintained in `api-docs.yaml`.

## Architecture Overview
- Request flow is `controller -> service -> repository`, with domain entities persisted through JPA repositories.
- Keep HTTP concerns (status codes, request parsing) in controllers and business rules in services.
- Repositories should focus on persistence/query logic only; avoid business decisions there.
- Use DTOs/forms as boundary models for request/response mapping.

## Build, Test, and Development Commands
- `./gradlew bootRun`: run the API server locally (default `http://localhost:8080`).
- `./gradlew clean test`: run all unit/integration tests on JUnit Platform (excludes `perf`-tagged tests).
- `./gradlew perfTest`: run performance tests tagged with `perf`.
- `./gradlew jacocoTestReport`: generate test coverage HTML report in `build/reports/jacoco/test/html/index.html`.
- `./gradlew build`: compile, test, and package the application.
- `./gradlew test --tests "*TeamServiceMatchingAlgorithmTest"`: run a focused test class during development.

## Coding Style & Naming Conventions
- Target Java 17 and Spring Boot 3.x.
- Use 4-space indentation and standard Java naming:
- Classes: `PascalCase` (`AcademicTermService`).
- Methods/fields: `camelCase` (`setCurrentTerm`).
- Constants: `UPPER_SNAKE_CASE`.
- Keep controllers thin; place business logic in services and persistence logic in repositories.
- Prefer DTO/form objects for API boundaries (`controller/form`, `dto`) instead of exposing domain entities directly.

## Testing Guidelines
- Testing stack: `spring-boot-starter-test` + Mockito.
- Name tests with `*Test` suffix and mirror production package structure.
- Add/adjust tests with every behavior change, especially in `service` and `controller` layers.
- Use `./gradlew clean test jacocoTestReport` before opening a PR; review coverage deltas in the JaCoCo report.

## Commit & Pull Request Guidelines
- Follow Conventional Commit style seen in history: `feat:`, `fix:`, `refactor:`, `test:`, `hotfix:`.
- Commit message format: `type: short imperative summary` (Korean or English is acceptable; be consistent within a PR).
- PRs should include what changed and why, linked issue/PR number (for example `#190`), API impact, and test evidence.

## Security & Configuration Tips
- Do not commit personal or environment-specific secrets.
- Keep `src/main/resources/application.yml` generic; use `application-local.yml` for local overrides (DB, secrets, origins).
- Validate sensitive config via environment variables (for example `JWT_SECRET`, `WEBHOOK_DISCORD`).

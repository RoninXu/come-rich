# Repository Guidelines

## Project Structure & Module Organization
- `finance-planner-backend/` is a multi-module Spring Boot workspace. Each domain lives in its own module (for example `finance-planner-auth/`, `finance-planner-accounting/`, `finance-planner-ai/`). The app entry point and Flyway migrations live in `finance-planner-app/src/main/resources/db/migration/`.
- `finance-planner-frontend/` is the Vue 3 SPA. Source is under `src/` with `api/`, `components/`, `router/`, `stores/`, `utils/`, and `views/`.
- `docs/plans/` holds product and technical documentation.
- `docker-compose-dev.yml` provides PostgreSQL and Redis for local development.

## Build, Test, and Development Commands
Run these from the repo root or the listed subdirectory:
- `docker-compose -f docker-compose-dev.yml up -d` to start PostgreSQL + Redis.
- `cd finance-planner-backend; mvn clean install` to build all backend modules.
- `cd finance-planner-backend; mvn spring-boot:run -pl finance-planner-app` to run the API.
- `cd finance-planner-frontend; pnpm install` and `pnpm dev` to run the SPA.
- `cd finance-planner-frontend; pnpm build` for a production build.

## Coding Style & Naming Conventions
- Database tables: lowercase snake_case (example: `financial_goal`).
- Java: PascalCase classes, camelCase methods, UPPER_SNAKE_CASE constants.
- Vue/TypeScript: PascalCase components, kebab-case file names.
- Formatting/linting: `pnpm lint` (ESLint) and `pnpm format` (Prettier).

## Testing Guidelines
- Backend tests use Spring Boot test (JUnit 5 via `spring-boot-starter-test`). Run `mvn test` or module-specific tests with `mvn test -pl <module>`.
- Frontend tests use Vitest. Run `pnpm test:unit` or `pnpm test:coverage`. Tests live alongside code in `__tests__/`.
- Add or update tests for any behavior change before opening a PR.

## Commit & Pull Request Guidelines
- Commit history uses Conventional Commits: `feat:`, `fix:`, `docs:`, `test:`, and `merge:` are common patterns.
- Work from feature branches named `feature/<name>`, `fix/<name>`, or `refactor/<scope>` and open PRs against `master`.
- PRs should include a clear summary, test results, and screenshots for UI changes. Link related issues when applicable.
- Keep `README.md`, `docs/plans/PROGRESS.md`, and `CLAUDE.md` in sync when user-facing features or architecture change.

## Configuration & Security
- Set local AI keys via environment variables (`DEEPSEEK_API_KEY`, `MOONSHOT_API_KEY`, `BAIDU_OCR_API_KEY`, `BAIDU_OCR_SECRET_KEY`).
- Do not commit secrets. Use `.env` or shell exports for local setup.

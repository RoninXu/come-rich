# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workflow Rules

- **Git Branch Protection**: NEVER make direct code changes on the `master` branch. All development must follow feature branch workflow:
  1. Before starting any new feature/task, ask the user if they want to create a new feature branch
  2. Suggested branch naming: `feature/<feature-name>`, `fix/<bug-name>`, `refactor/<scope>`
  3. All code changes must be committed to the feature branch
  4. After completing the feature, ask the user if they want to create a Pull Request to merge into `master`

- **Git Commit Prompt**: After any file changes (create, edit, delete), always ask the user if they want to commit the changes to git before proceeding to the next task.

- **Pull Request Workflow**: After committing code to a feature branch:
  1. Ask the user if they want to create a PR for code review and merge
  2. Use `gh pr create` to create the PR with a clear title and description
  3. Wait for user approval before merging

- **Code Verification**: After any code update (new files, edits, or component additions), run the appropriate verification checks to ensure the code works correctly:
  - **Backend changes**: Run `mvn compile` or `mvn spring-boot:run` to verify compilation and startup
  - **Frontend changes**: Run `pnpm install` (if dependencies changed) and `pnpm dev` to verify the app starts
  - **Database changes**: Verify database connection and run Flyway migrations
  - **Docker changes**: Run `docker-compose -f docker-compose-dev.yml up -d` to verify containers start
  - **API changes**: Test endpoints using Swagger UI or curl commands
  - Report any errors to the user before proceeding

- **Global File Sync**: After any code changes, always update the following global files before committing:
  1. **`README.md`** — Features, Project Structure, Tech Stack, Getting Started, etc.
  2. **`docs/plans/PROGRESS.md`** — Development progress, completed/pending items, module table, page routes
  3. **`CLAUDE.md`** — Current Status, Project Structure, Module Architecture, Frontend Views
  4. Keep all three files consistent with each other and with the actual codebase
  5. Update these files as part of the same commit or PR — never leave them stale

- **Unit Testing Requirements**: All code changes must include corresponding unit tests:
  1. **When writing new code**: Create unit tests for all new classes, methods, and components
  2. **When modifying existing code**: Update or add tests to cover the changes
  3. **Before completing development**: Run all unit tests and ensure they pass
  4. **Test commands**:
     - Backend: `mvn test` (run all tests) or `mvn test -pl <module-name>` (run module tests)
     - Frontend: `pnpm test` or `pnpm test:unit`
  5. **Test coverage expectations**:
     - Service classes: Test all public methods with various inputs
     - Controller classes: Test request/response handling and error cases
     - Utility classes: Test edge cases and boundary conditions
  6. **NEVER commit code with failing tests** - fix all test failures before committing

## Project Overview

Come Rich (AI个人理财规划师) is an AI-powered personal finance planning application targeting Chinese middle-class users. The project helps users track expenses, receive AI-driven financial coaching, and discover income growth opportunities.

**Current Status**: Phase 2 AI chat module completed - Multi-model LLM integration, SSE streaming, conversation history, rate limiting, full chat UI.

## Technology Stack

### Backend
- Java 17 + Spring Boot 3.2.x
- Spring Data JPA + PostgreSQL 14.x
- Spring Data Redis + Redis 7.x
- Spring Security + JWT authentication
- Spring WebFlux for SSE streaming (AI chat)
- DeepSeek V3 API for AI capabilities
- Baidu OCR API for receipt recognition
- Flyway for database migrations
- Maven 3.9.x

### Frontend
- Vue 3.4.x + TypeScript 5.x
- Vite 5.x build tool
- Element Plus UI components
- Pinia state management
- Vue Router 4.x
- Axios for HTTP requests
- ECharts for data visualization

### Infrastructure
- Docker + Docker Compose
- Nginx reverse proxy
- Aliyun cloud services (ECS, OSS, RDS)

## Project Structure

```
come-rich/
├── docs/plans/                           # Project documentation
│   ├── AI理财规划师PRD.md                 # Product requirements document
│   └── AI理财规划师-技术文档.md            # Technical implementation specs
├── finance-planner-backend/              # Backend (Spring Boot)
│   ├── pom.xml                           # Parent POM
│   ├── finance-planner-common/           # Shared utilities, exceptions
│   ├── finance-planner-auth/             # Authentication & authorization
│   ├── finance-planner-accounting/       # Transaction management
│   ├── finance-planner-analysis/         # Statistics & health score
│   ├── finance-planner-ai/              # AI chat & multi-model LLM integration
│   └── finance-planner-app/              # Main application entry
│       └── src/main/resources/
│           ├── application.yml
│           └── db/migration/             # Flyway SQL scripts
├── finance-planner-frontend/             # Frontend (Vue 3)
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── api/                          # API request modules
│       ├── components/                   # Reusable components
│       ├── router/                       # Vue Router config
│       ├── stores/                       # Pinia state management
│       ├── utils/                        # Utilities (request, auth)
│       └── views/                        # Page components
├── docker-compose-dev.yml                # Development environment
├── .claude/                              # Claude Code configuration
└── CLAUDE.md                             # This file
```

## Module Architecture

### Backend Modules
- `finance-planner-common` - Shared utilities, constants, exceptions, ApiResponse
- `finance-planner-auth` - User authentication, JWT, Spring Security
- `finance-planner-accounting` - Transaction CRUD, category management
- `finance-planner-analysis` - Monthly statistics, health score calculation
- `finance-planner-ai` - AI chat, multi-model LLM integration (DeepSeek, Moonshot), SSE streaming
- `finance-planner-career` - Side hustle recommendations (Phase 2 Advanced - planned)
- `finance-planner-goal` - Goal planning and tracking (Phase 2 Advanced - planned)

### Frontend Views
- `auth/` - Login and registration pages
- `dashboard/` - Home dashboard with summary
- `accounting/` - Transaction list and form
- `analysis/` - Monthly report and health score
- `ai/` - AI financial advisor chat with SSE streaming

## Development Commands

### Start Development Environment
```bash
# Start PostgreSQL and Redis
docker-compose -f docker-compose-dev.yml up -d

# Backend (from finance-planner-backend/)
mvn clean install
mvn spring-boot:run -pl finance-planner-app

# Frontend (from finance-planner-frontend/)
pnpm install
pnpm dev
```

### Database
- PostgreSQL: `localhost:5432`, database: `finance_planner`, user: `finance`, password: `finance123`
- Redis: `localhost:6379`

### API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## Code Conventions

- **Database**: Table names lowercase with underscores (e.g., `financial_goal`)
- **Java**: Classes in PascalCase, methods in camelCase, constants in UPPER_SNAKE_CASE
- **TypeScript/Vue**: Components in PascalCase, files in kebab-case
- **Git Commits**: Use conventional commits (feat:, fix:, docs:, refactor:)

## Key Documentation

Before implementing features, review:
- **PRD** (`docs/plans/AI理财规划师PRD.md`) - Product requirements, user personas, feature specifications
- **Tech Doc** (`docs/plans/AI理财规划师-技术文档.md`) - Architecture, database schema, API design, code examples

## AI Integration Notes

- DeepSeek API is used for AI chat functionality
- AI responses must include risk disclaimers for financial advice
- Never recommend specific financial products - only suggest "investment categories"
- Implement rate limiting: free users 10 chats/day, paid users unlimited

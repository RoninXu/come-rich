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
  1. **`README.md`** - Features, Project Structure, Tech Stack, Getting Started, etc.
  2. **`docs/plans/PROGRESS.md`** - Development progress, completed/pending items, module table, page routes
  3. **`CLAUDE.md`** - Current Status, Project Structure, Module Architecture, Frontend Views
  4. Keep all three files consistent with each other and with the actual codebase
  5. Update these files as part of the same commit or PR - never leave them stale

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

Come Rich（AI个人理财规划师）面向中国中产用户的个人理财规划应用，覆盖记账、目标、预算、投资建议与职业/收入增长建议，并提供 AI 助手与 OCR 票据识别能力。

**Current Status**: Phase 5.1 (Copilot 风格 UI/UX 全站重构第一版) completed on 2026-02-06. Phase 1-4 features remain fully delivered.
**Recent Update**: Frontend visual refresh with new token system and layout shell, top navigation + secondary nav grouping, redesigned Dashboard/Transactions/Budget/AI Chat/Auth pages, new shared components (MetricCard/BudgetProgressBar/MoneyText/SectionHeader/InsightPanel/EmptyState), and additional frontend unit tests.

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
- Naive UI components
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
|-- .env.example                          # Backend environment template
|-- .env.docker.example                   # Docker environment template
|-- docs/
|   |-- plans/                            # Project documentation
|   |   |-- AI理财规划师PRD.md             # Product requirements document
|   |   `-- AI理财规划师-技术文档.md        # Technical implementation specs
|   |-- AI_AGENT.md                        # AI & AI Agent technical doc
|   `-- ENV_CONFIGURATION.md              # Environment configuration guide
|-- finance-planner-backend/              # Backend (Spring Boot)
|   |-- pom.xml                           # Parent POM
|   |-- finance-planner-common/           # Shared utilities, exceptions
|   |-- finance-planner-auth/             # Authentication & authorization
|   |-- finance-planner-accounting/       # Transaction management
|   |-- finance-planner-analysis/         # Statistics & health score
|   |-- finance-planner-ai/               # AI chat, multi-model LLM, OCR import
|   |-- finance-planner-goal/             # Financial goal planning & tracking
|   |-- finance-planner-career/           # Side hustle recommendations & income
|   |-- finance-planner-budget/           # Budget management & AI optimization
|   |-- finance-planner-investment/       # Risk assessment & investment advice
|   `-- finance-planner-app/              # Main application entry
|       `-- src/main/resources/
|           |-- application.yml           # Main config (uses env vars)
|           |-- application-prod.yml      # Production profile
|           `-- db/migration/             # Flyway SQL scripts
|-- finance-planner-frontend/             # Frontend (Vue 3)
|   |-- .env.example                      # Frontend environment template
|   |-- .env.development                  # Development config
|   |-- .env.production                   # Production config
|   |-- package.json
|   |-- vite.config.ts
|   `-- src/
|       |-- api/                          # API request modules
|       |-- components/                   # Reusable components
|       |-- router/                       # Vue Router config
|       |-- stores/                       # Pinia state management
|       |-- utils/                        # Utilities (request, auth, env)
|       `-- views/                        # Page components
|-- docker-compose-dev.yml                # Development environment
|-- .claude/                              # Claude Code configuration
`-- CLAUDE.md                             # This file
```

## Module Architecture

### Backend Modules
- `finance-planner-common` - Shared utilities, constants, exceptions, ApiResponse
- `finance-planner-auth` - User authentication, JWT, Spring Security
- `finance-planner-accounting` - Transaction CRUD, category management
- `finance-planner-analysis` - Monthly statistics, health score calculation
- `finance-planner-ai` - AI chat, multi-model LLM integration (DeepSeek, Moonshot), SSE streaming, OCR bill import
- `finance-planner-goal` - Financial goal planning, progress tracking, AI savings plans
- `finance-planner-career` - Side hustle recommendations, user profile, income tracking, AI startup plans
- `finance-planner-budget` - Budget management per category, budget-vs-actual comparison, AI optimization suggestions
- `finance-planner-investment` - Risk assessment quiz (8 questions), AI investment track recommendations, asset allocation

### Frontend Views
- `auth/` - Login and registration pages
- `dashboard/` - Home dashboard with summary, budget utilization card, risk profile card
- `accounting/` - Transaction list (with Excel/CSV export), form, and OCR import page
- `analysis/` - Monthly report (with export) and health score (with export)
- `ai/` - AI financial advisor chat with SSE streaming
- `goal/` - Goal list, form, and detail pages with progress charts
- `career/` - Profile, AI recommendations, plan list, and plan detail pages
- `budget/` - Budget overview, budget form (batch edit), budget trend chart
- `investment/` - Investment advice (pie chart, recommendations), risk quiz (8-step), assessment history

## Development Commands

### Environment Setup
```bash
# Copy environment template and configure
cp .env.example .env
# Edit .env with your actual values (DB_PASSWORD, JWT_SECRET, API keys)

# See docs/ENV_CONFIGURATION.md for detailed setup guide
```

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
- PostgreSQL: `localhost:5432`, database: `finance_planner`, user: `finance`, password: (from .env)
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
- **AI & Agent Doc** (`docs/AI_AGENT.md`) - AI chat/agent architecture, tools, SSE, and flows

## AI Integration Notes

- DeepSeek API is used for AI chat functionality
- AI responses must include risk disclaimers for financial advice
- Never recommend specific financial products - only suggest "investment categories"
- Implement rate limiting: free users 10 chats/day, paid users unlimited

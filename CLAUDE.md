# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Workflow Rules

- **Git Commit Prompt**: After any file changes (create, edit, delete), always ask the user if they want to commit the changes to git before proceeding to the next task.

## Project Overview

Come Rich (AI个人理财规划师) is an AI-powered personal finance planning application targeting Chinese middle-class users. The project helps users track expenses, receive AI-driven financial coaching, and discover income growth opportunities.

**Current Status**: Planning/Design phase with comprehensive documentation completed. Source code implementation pending.

## Technology Stack

### Backend (Planned)
- Java 17 + Spring Boot 3.2.x
- Spring Data JPA + PostgreSQL 14.x
- Spring Data Redis + Redis 7.x
- Spring Security + JWT authentication
- Spring WebFlux for SSE streaming (AI chat)
- DeepSeek V3 API for AI capabilities
- Baidu OCR API for receipt recognition
- Maven 3.9.x

### Frontend (Planned)
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
├── docs/plans/                    # Project documentation
│   ├── AI理财规划师PRD.md          # Product requirements document
│   └── AI理财规划师-技术文档.md      # Technical implementation specs
├── .claude/                       # Claude Code configuration
└── CLAUDE.md                      # This file
```

## Planned Module Architecture

### Backend Modules
- `finance-planner-common` - Shared utilities, constants, exceptions
- `finance-planner-auth` - User authentication and authorization
- `finance-planner-accounting` - Expense tracking and OCR recognition
- `finance-planner-ai` - AI conversations and DeepSeek integration
- `finance-planner-analysis` - Financial analytics and health scoring
- `finance-planner-career` - Side hustle recommendations
- `finance-planner-goal` - Goal planning and tracking
- `finance-planner-notification` - Alerts and notifications

### Frontend Views
- `auth/` - Login and registration
- `accounting/` - Manual and photo-based expense recording
- `analysis/` - Dashboard and financial reports
- `ai/` - AI chatbot interface
- `career/` - Side hustle recommendations
- `goal/` - Goal management

## Key Documentation

Before implementing features, review:
- **PRD** (`docs/plans/AI理财规划师PRD.md`) - Product requirements, user personas, feature specifications
- **Tech Doc** (`docs/plans/AI理财规划师-技术文档.md`) - Architecture, database schema, API design, code examples

## Development Guidelines

### When Starting Development

1. **Backend Project Initialization**:
   ```bash
   # Use Spring Initializr or IDEA to create project with dependencies:
   # Spring Web, Spring Security, Spring Data JPA, PostgreSQL Driver,
   # Spring Data Redis, Lombok, Validation
   ```

2. **Frontend Project Initialization**:
   ```bash
   pnpm create vite finance-planner-frontend --template vue-ts
   cd finance-planner-frontend
   pnpm add element-plus @element-plus/icons-vue axios vue-router@4 pinia dayjs echarts
   ```

3. **Docker Development Environment**:
   ```bash
   # Start PostgreSQL and Redis
   docker-compose -f docker-compose-dev.yml up -d
   ```

### Code Conventions

- **Database**: Table names lowercase with underscores (e.g., `financial_goal`)
- **Java**: Classes in PascalCase, methods in camelCase, constants in UPPER_SNAKE_CASE
- **TypeScript/Vue**: Components in PascalCase, files in kebab-case
- **Git Commits**: Use conventional commits (feat:, fix:, docs:, refactor:)

### AI Integration Notes

- DeepSeek API is used for AI chat functionality
- AI responses must include risk disclaimers for financial advice
- Never recommend specific financial products - only suggest "investment categories"
- Implement rate limiting: free users 10 chats/day, paid users unlimited

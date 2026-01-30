# Come Rich - AI Personal Finance Planner

AI-powered personal finance planning application targeting Chinese middle-class users. Track expenses, receive AI-driven financial coaching, and discover income growth opportunities.

## Features

**Phase 1 (Completed)**
- User authentication (registration, login, JWT)
- Category management (income/expense categories with icons)
- Transaction management (CRUD, filtering, pagination)
- Monthly analysis (statistics, spending breakdown, financial health score)
- Full frontend integration with Vue 3 dashboard

**Phase 2 — AI Chat (Completed)**
- AI financial advisor chat with SSE streaming
- Multi-model LLM support (DeepSeek, Moonshot, etc.) with runtime switching
- Financial context injection (monthly stats + health score into AI prompts)
- Conversation history persistence
- Redis-based daily rate limiting (10 chats/day)
- Full chat UI with typing indicator, quick actions, markdown rendering

**Phase 2 — Advanced (Planned)**
- Receipt OCR recognition via Baidu OCR
- Financial goal planning and tracking
- Side hustle / career recommendations

## Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security, JWT, WebFlux (WebClient) |
| Frontend | Vue 3.4, TypeScript 5, Vite 5, Element Plus, Pinia, ECharts |
| Database | PostgreSQL 14, Redis 7, Flyway migrations |
| AI | DeepSeek V3 API, Baidu OCR API |
| Infra | Docker Compose, Nginx, Aliyun (ECS, OSS, RDS) |

## Project Structure

```
come-rich/
├── finance-planner-backend/          # Spring Boot multi-module backend
│   ├── finance-planner-common/       # Shared utilities, exceptions, API response
│   ├── finance-planner-auth/         # Authentication & JWT security
│   ├── finance-planner-accounting/   # Transaction & category management
│   ├── finance-planner-analysis/     # Statistics & health score
│   ├── finance-planner-ai/          # AI chat & multi-model LLM integration
│   └── finance-planner-app/          # Main application entry & Flyway migrations
├── finance-planner-frontend/         # Vue 3 + TypeScript SPA
│   └── src/
│       ├── api/                      # API request modules
│       ├── components/               # Reusable UI components
│       ├── router/                   # Vue Router configuration
│       ├── stores/                   # Pinia state management
│       ├── utils/                    # Utilities (HTTP client, auth helpers)
│       └── views/                    # Page components
├── docs/plans/                       # PRD and technical documentation
├── docker-compose-dev.yml            # Dev environment (PostgreSQL + Redis)
└── CLAUDE.md                         # AI assistant instructions
```

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 18+ with pnpm
- Docker & Docker Compose

## Getting Started

### 1. Start infrastructure services

```bash
docker-compose -f docker-compose-dev.yml up -d
```

This starts PostgreSQL (port 5432) and Redis (port 6379).

### 2. Build and run the backend

```bash
cd finance-planner-backend
mvn clean install
mvn spring-boot:run -pl finance-planner-app
```

Backend runs at `http://localhost:8080`. Flyway auto-applies database migrations on startup.

For AI chat functionality, set the LLM API key environment variable:

```bash
export DEEPSEEK_API_KEY=your_api_key_here
# Or for Moonshot:
# export MOONSHOT_API_KEY=your_api_key_here
```

### 3. Run the frontend

```bash
cd finance-planner-frontend
pnpm install
pnpm dev
```

Frontend runs at `http://localhost:5173` with API proxy to backend.

## Development

### Running Tests

```bash
# Backend - all modules
cd finance-planner-backend
mvn test

# Backend - specific module
mvn test -pl finance-planner-auth

# Frontend
cd finance-planner-frontend
pnpm test:unit
```

### API Documentation

Once the backend is running:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

### Database

| Service | Host | Port | Database | User | Password |
|---|---|---|---|---|---|
| PostgreSQL | localhost | 5432 | finance_planner | finance | finance123 |
| Redis | localhost | 6379 | - | - | - |

### Code Conventions

- **Database**: lowercase with underscores (`financial_goal`)
- **Java**: PascalCase classes, camelCase methods, UPPER_SNAKE_CASE constants
- **TypeScript/Vue**: PascalCase components, kebab-case filenames
- **Git**: conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`)

## Contributing

1. Create a feature branch from `master` (`feature/<name>`, `fix/<name>`, `refactor/<name>`)
2. Make changes with corresponding unit tests
3. Ensure all tests pass before committing
4. Create a Pull Request to `master`

## License

Private project.

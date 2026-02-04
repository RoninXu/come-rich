# Repository Guidelines

## 项目概述
Come Rich（AI个人理财规划师）面向中国中产用户的个人理财规划应用，覆盖记账、目标规划、预算管理、投资建议、职业与收入增长建议，并提供 AI 助理与 OCR 票据识别能力。系统以“数据记录 + 分析评估 + AI 辅助决策”的流程帮助用户提升财务健康度。

## 技术栈
### 后端
- Java 17 + Spring Boot 3.2.x
- Spring Data JPA + PostgreSQL 14.x
- Spring Data Redis + Redis 7.x
- Spring Security + JWT 认证
- Spring WebFlux（SSE 流式输出）
- DeepSeek V3 API / Moonshot API（AI 对话）
- 百度 OCR API（票据识别）
- Flyway 数据库迁移
- Maven 3.9.x

### 前端
- Vue 3.4.x + TypeScript 5.x
- Vite 5.x
- Element Plus UI
- Pinia 状态管理
- Vue Router 4.x
- Axios
- ECharts

### 基础设施
- Docker + Docker Compose
- Nginx 反向代理
- 阿里云 ECS/OSS/RDS（部署）

## 架构设计
### 整体结构
- 前后端分离：前端 SPA 调用后端 REST/SSE API
- 模块化后端：按业务域拆分为独立 Spring Boot 模块
- 统一网关与鉴权：JWT + Spring Security
- 数据层：PostgreSQL 为主存储，Redis 缓存与会话
- AI 能力：多模型适配层 + SSE 实时流式响应

### 后端模块
- `finance-planner-common`：通用工具、异常、响应结构
- `finance-planner-auth`：用户认证、JWT、安全配置
- `finance-planner-accounting`：交易流水、分类管理
- `finance-planner-analysis`：统计报表、财务健康评分
- `finance-planner-ai`：AI 对话、多模型接入、OCR 导入
- `finance-planner-goal`：目标规划、进度追踪
- `finance-planner-career`：副业/职业建议、收入跟踪
- `finance-planner-budget`：预算管理、预算对比与优化
- `finance-planner-investment`：风险评测、投资建议
- `finance-planner-app`：应用入口与配置、Flyway 迁移

### 前端页面
- `auth/`：登录、注册
- `dashboard/`：仪表盘、卡片汇总
- `accounting/`：记账列表、表单、OCR 导入
- `analysis/`：月度报告、健康评分
- `ai/`：AI 理财顾问对话
- `goal/`：目标列表、详情、进度图表
- `career/`：画像、推荐、计划列表/详情
- `budget/`：预算概览、批量编辑、趋势图
- `investment/`：投资建议、风险测评历史

## 项目结构（Project Structure）
```
come-rich/                                # 仓库根目录
├── docs/                                 # 项目文档根目录
│   └── plans/                            # 需求与技术文档
│       ├── AI理财规划师PRD.md             # 产品需求说明
│       └── AI理财规划师-技术文档.md        # 技术实现规格
├── finance-planner-backend/              # 后端（Spring Boot 多模块）
│   ├── pom.xml                           # 后端父 POM
│   ├── finance-planner-common/           # 公共工具、异常、响应
│   ├── finance-planner-auth/             # 认证与授权模块
│   ├── finance-planner-accounting/       # 记账与交易流水
│   ├── finance-planner-analysis/         # 统计报表与健康评分
│   ├── finance-planner-ai/               # AI 对话、OCR、多模型
│   ├── finance-planner-goal/             # 理财目标与进度
│   ├── finance-planner-career/           # 职业画像与收入建议
│   ├── finance-planner-budget/           # 预算管理与优化
│   ├── finance-planner-investment/       # 风险评测与投资建议
│   └── finance-planner-app/              # 应用入口与配置
│       └── src/main/resources/db/migration/ # Flyway 数据库迁移脚本
├── finance-planner-frontend/             # 前端（Vue 3 SPA）
│   ├── package.json                      # 前端依赖与脚本
│   ├── vite.config.ts                    # Vite 构建配置
│   └── src/                              # 前端源码
│       ├── api/                          # 接口请求封装
│       ├── components/                   # 通用组件
│       ├── router/                       # 路由配置与守卫
│       ├── stores/                       # Pinia 状态管理
│       ├── utils/                        # 工具函数
│       └── views/                        # 页面视图
├── docker-compose-dev.yml                # 本地 PostgreSQL + Redis
├── README.md                             # 项目总览与使用说明
└── CLAUDE.md                             # Claude 协作规则
```

## 模块架构（Module Architecture）
### 后端模块职责
- `common`：统一响应、异常体系、工具类、常量
- `auth`：用户认证、JWT、安全策略
- `accounting`：收支流水、分类与账目管理
- `analysis`：统计报表、财务健康评分
- `ai`：AI 对话、多模型适配、OCR 导入、SSE 流式输出
- `goal`：理财目标、进度追踪与计划
- `career`：职业画像、收入增长建议、计划管理
- `budget`：预算设置、对比与优化建议
- `investment`：风险评测、资产配置与建议
- `app`：应用启动入口、统一配置、Flyway 迁移

### 前端模块职责
- `api`：接口封装与请求管理
- `components`：通用组件
- `router`：路由配置与守卫
- `stores`：Pinia 状态管理
- `utils`：工具与通用方法
- `views`：业务页面实现

## 全局规则（Workflow Rules）
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

# Come Rich - AI Personal Finance Planner（AI个人理财规划师）

面向中国中产用户的 AI 个人理财规划应用。以“记录 → 分析 → 规划 → 行动”为主线，帮助用户看清收支结构、设定财务目标、优化预算与资产配置，并通过 AI 提供可执行的理财建议与收入增长方案。

## 项目价值（Why It Matters）
- 全链路闭环：从记账、目标、预算、投资到职业增长建议，一站式覆盖个人财务管理关键场景
- AI 辅助决策：基于用户账本与画像生成个性化建议，降低理财门槛
- 可执行输出：不仅给建议，还给计划（如 90 天行动方案、预算优化策略）
- 可解释与可落地：结合统计报表与健康评分，清晰展示财务现状与改进路径

## 最新进展（Current Status）
Phase 4.3（AI Agent 高级功能增强）已完成；Phase 5.1（Copilot 风格 UI/UX 重构）已完成第一版全量替换：
- 前端视觉系统重构：主题令牌、CSS 变量、Naive UI 主题覆盖统一升级
- 壳层与导航重构：从重侧栏改为顶部主导航 + 二级导航分组
- 核心页面重做：Dashboard、交易记录、预算总览、AI 顾问、登录/注册
- 新增通用组件：`MetricCard`、`BudgetProgressBar`、`MoneyText`、`SectionHeader`、`InsightPanel`、`EmptyState`
- 文案与路由标题统一中文修复，减少编码异常风险
- 前端单测新增并通过（109/109）
- Phase 5.2（Agent 分析仪表盘）第一版已完成：
  - 新增 Agent 指标聚合 API：`/api/ai/agent/metrics/overview|tools|timeline|errors`
  - 新增 Agent 分析页：`/ai/agent-metrics`（指标卡、趋势图、错误分布、工具排行）
  - 新增后端/前端单测覆盖指标查询与路由/API 调用

Phase 1-4 功能已全部交付：
- AI Agent 高级增强：多 Provider 自动降级、工具结果 Redis 缓存、Rate Limiting 预留机制、错误恢复、指标埋点
- AI Agent 工具扩展：28 个工具（记账 6 + 分析 3 + 预算 6 + 目标 6 + 投资 3 + 副业 4）
- AI Agent 基础能力：工具体系、ReAct 编排、SSE 协议升级、确认流程、上下文压缩
- 预算管理：按分类月度预算、预算 vs 实际对比、AI 优化建议
- 投资建议：8 题风险测评、AI 资产配置建议
- 数据导出：交易 Excel/CSV 导出、月度/年度报表导出

## 核心功能
**Phase 1（已完成）**
- 用户认证（注册、登录、JWT）
- 收支分类管理（含图标）
- 交易流水管理（CRUD、筛选、分页）
- 月度分析（统计、支出分布、财务健康评分）
- Vue 3 前端仪表盘

**Phase 2（AI 能力，已完成）**
- AI 理财顾问对话（SSE 流式）
- 多模型 LLM 支持（DeepSeek、Moonshot）与运行时切换
- 财务上下文注入（统计 + 健康评分进入提示词）
- 对话历史持久化
- Redis 日限流（10 次/天）
- 完整聊天 UI（打字机、快捷动作、Markdown 渲染）

**Phase 2 进阶（已完成）**
- 理财目标规划（进度跟踪、AI 储蓄计划、ECharts 可视化）
- 票据 OCR 识别（百度 OCR + 自动分类 + 生成流水）
- 职业/副业建议（AI 匹配、90 天计划、收入跟踪）

**Phase 3（预算、投资与导出，已完成）**
- 预算管理：分类预算、预算对比、AI 优化
- 投资建议：风险测评、资产配置建议
- 数据导出：Excel/CSV、月度/年度报表

## Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Data JPA, Spring Security, JWT, WebFlux (SSE) |
| Frontend | Vue 3.4, TypeScript 5, Vite 5, Naive UI, Pinia, ECharts |
| Database | PostgreSQL 14, Redis 7, Flyway |
| AI | DeepSeek V3 API, Moonshot API, Baidu OCR API, Apache POI |
| Infra | Docker Compose, Nginx, Aliyun (ECS/OSS/RDS) |

## 项目结构（Project Structure）

```
come-rich/
├── .env.example                          # 环境变量模板（后端）
├── .env.docker.example                   # Docker 环境变量模板
├── docs/                                 # 项目文档
│   ├── plans/                            # 需求与技术文档
│   ├── AI_AGENT.md                        # AI 与 AI Agent 技术文档
│   └── ENV_CONFIGURATION.md              # 环境配置指南
├── finance-planner-backend/              # 后端（Spring Boot 多模块）
│   ├── finance-planner-common/           # 公共工具与响应结构
│   ├── finance-planner-auth/             # 认证与授权
│   ├── finance-planner-accounting/       # 记账与交易流水
│   ├── finance-planner-analysis/         # 统计报表与健康评分
│   ├── finance-planner-ai/               # AI 对话、OCR、多模型
│   ├── finance-planner-goal/             # 理财目标与进度
│   ├── finance-planner-career/           # 职业画像与收入建议
│   ├── finance-planner-budget/           # 预算管理与优化
│   ├── finance-planner-investment/       # 风险评测与投资建议
│   └── finance-planner-app/              # 应用入口与 Flyway 迁移
└── finance-planner-frontend/             # 前端（Vue 3 SPA）
    ├── .env.example                      # 前端环境变量模板
    ├── .env.development                  # 开发环境配置
    ├── .env.production                   # 生产环境配置
    └── src/                              # 前端源码（api/components/router/stores/utils/views）
```

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 18+ with pnpm
- Docker & Docker Compose

## Getting Started

### 1. Environment Configuration

```bash
# Copy environment template and fill in your values
cp .env.example .env

# Edit .env with your actual configuration
# Required: DB_PASSWORD, JWT_SECRET, DEEPSEEK_API_KEY
```

See [Environment Configuration Guide](docs/plans/ENV_CONFIGURATION.md) for detailed setup instructions.

### 2. Start infrastructure services

```bash
docker-compose -f docker-compose-dev.yml up -d
```

### 3. Build and run the backend

```bash
cd finance-planner-backend
mvn clean install
mvn spring-boot:run -pl finance-planner-app
```

Backend runs at `http://localhost:8080`. Flyway auto-applies database migrations on startup.

### 4. Run the frontend

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

# Come Rich 开发进度

## 当前状态

**阶段**: Phase 4.1 - AI Agent 基础能力 完成
**最后更新**: 2026-02-04

- 修复 AI Agent 记账相对日期解析（今天/昨天/明天）
- 强化 AI Agent 记账日期覆盖逻辑（相对日期优先于模型输出）
- 新增通义千问（Qwen）模型配置选项
- 新增 /api/ai/session-status（时间上下文）与个人资料时区设置
- 优化 AI 次数用尽提示（前端友好文案）
- 修复 SSE 速率限制错误提示解析

## 已完成

### Phase 1 - Step 1-6 (基础架构) ✅

- [x] Docker 开发环境配置 (PostgreSQL + Redis)
- [x] 后端 Maven 多模块项目结构
- [x] 公共模块 (ApiResponse, ErrorCode, GlobalExceptionHandler)
- [x] 数据库迁移脚本 (Flyway V1-V4)
- [x] 认证模块 (JWT, Spring Security, 登录/注册)
- [x] 前端 Vue 3 项目结构
- [x] 前端基础架构 (Axios, Router, Pinia)
- [x] 登录/注册页面
- [x] 仪表盘首页
- [x] 交易记录列表和表单页面 (UI完成，API待接入)
- [x] 月度报表页面 (UI完成，API待接入)
- [x] 财务健康评分页面 (UI完成，API待接入)

### Phase 1 - Step 7-12 (业务功能) ✅

- [x] **Step 7: 分类模块**
  - [x] Category 实体和 Repository
  - [x] CategoryService + CategoryController
  - [x] GET /api/categories, GET /api/categories/tree
  - [x] 前端 CategorySelector 组件接入真实数据

- [x] **Step 8: 交易记录模块**
  - [x] Transaction 实体和 Repository
  - [x] TransactionService + TransactionController
  - [x] CRUD API: POST/GET/PUT/DELETE /api/transactions
  - [x] 分页查询和筛选

- [x] **Step 9: 前端记账功能接入**
  - [x] 接入分类 API
  - [x] 接入交易 CRUD API
  - [x] 完善表单验证和错误处理

- [x] **Step 10: 统计分析模块**
  - [x] StatisticsService (月度汇总、分类统计、每日统计)
  - [x] HealthScoreService (5维评分算法实现)
  - [x] AnalysisController
  - [x] GET /api/analysis/monthly
  - [x] GET /api/analysis/category
  - [x] GET /api/analysis/daily
  - [x] GET /api/analysis/health-score
  - [x] GET /api/analysis/dashboard

- [x] **Step 11: 前端统计页面接入**
  - [x] 月度报表接入真实数据 (收支汇总、分类饼图、日趋势图)
  - [x] 健康评分接入真实数据 (5维度评分明细、改进建议)
  - [x] 仪表盘首页接入真实数据 (收支卡片、健康分、最近记录)

- [x] **Step 12: 优化和完善**
  - [x] 编辑/删除交易记录功能 (含确认对话框)
  - [x] 日期范围筛选 (含快捷日期：今天/本周/本月)
  - [x] 加载状态 (v-loading 指令)
  - [x] 空状态组件 (el-empty)
  - [x] 错误提示优化 (ElMessage)

### Phase 2 - AI 对话模块 ✅

- [x] **AI 对话模块**
  - [x] 多模型 LLM 集成 (OpenAI 兼容接口: DeepSeek, Moonshot 等)
  - [x] SSE 流式响应 (SseEmitter + WebClient)
  - [x] 对话历史持久化 (ai_conversation 表, Flyway V5)
  - [x] AI 财务建议功能 (系统 Prompt + 用户财务上下文注入)
  - [x] 运行时模型切换 (LlmProviderManager)
  - [x] Redis 每日速率限制 (10次/天)
  - [x] JWT query param 回退 (SSE EventSource 兼容)
  - [x] 前端聊天页面 (消息气泡、打字指示器、快捷词、Markdown 渲染)
  - [x] 前端模型切换下拉框
  - [x] 37 个后端单元测试 + 12 个前端测试

### Phase 2 - 高级功能 ✅

- [x] **理财目标规划**
  - [x] 目标 CRUD (创建/查看/编辑/删除)
  - [x] 进度记录和自动完成检测
  - [x] ECharts 存款趋势图
  - [x] AI 理财计划生成 (DeepSeek)
  - [x] 目标列表 (状态筛选、优先级标签、进度条)
  - [x] 目标详情 (统计卡片、存款时间线、AI 建议面板)
  - [x] 15 个后端单元测试
  - [x] Flyway V6 迁移 (financial_goal + goal_progress 表)

- [x] **账单导入 (OCR)**
  - [x] Baidu OCR API 集成 (通用文字识别)
  - [x] 票据金额/商户/日期自动提取 (正则)
  - [x] 智能分类建议 (关键词匹配)
  - [x] OCR 预览/确认/拒绝工作流
  - [x] 拖拽上传界面 + 待处理列表
  - [x] 仪表盘和记账页快捷入口
  - [x] 8 个后端单元测试
  - [x] Flyway V7 迁移 (ocr_record 表)

- [x] **副业推荐**
  - [x] 用户资料管理 (职业/技能/经验/时间/兴趣)
  - [x] AI 副业推荐 (DeepSeek, 匹配度评分)
  - [x] 副业计划 CRUD + 状态管理 (探索/进行/暂停/完成)
  - [x] 收入记录和月度收入统计
  - [x] AI 90天启动计划生成
  - [x] ECharts 月度收入趋势图
  - [x] 推荐卡片页面 (匹配度环形图、采纳对话框)
  - [x] 计划列表和详情页面
  - [x] 7 个后端单元测试
  - [x] Flyway V8 迁移 (user_profile + career_plan + career_income 表)

### Phase 3 - 预算管理 + 投资建议 + 数据导出 ✅

- [x] **预算管理**
  - [x] 预算 CRUD (按分类设置月度预算)
  - [x] 预算总额设置
  - [x] 预算 vs 实际对比 (使用率计算、超支检测)
  - [x] 复制上月预算
  - [x] 预算趋势 (近6月对比)
  - [x] AI 预算优化建议 (DeepSeek)
  - [x] 预算概览页 (汇总卡片、分类表格、进度条、ECharts 柱状图)
  - [x] 预算设置表单页 (分类批量编辑)
  - [x] 预算趋势页 (折线图 + 对比表格)
  - [x] 10 个后端单元测试
  - [x] Flyway V9 迁移 (budget + budget_total 表)

- [x] **投资建议引擎**
  - [x] 风险评估问卷 (8题, 0-32分, 3档风险等级)
  - [x] 风险评估历史记录
  - [x] AI 投资组合推荐 (DeepSeek, 3-5个投资方向)
  - [x] 资产配置饼图数据
  - [x] 风险评估页 (el-steps 8步问卷)
  - [x] 投资建议页 (风险卡片、ECharts 饼图、推荐列表、免责声明)
  - [x] 评估历史页 (el-timeline)
  - [x] 7 个后端单元测试
  - [x] Flyway V10 迁移 (risk_assessment + investment_recommendation 表)

- [x] **数据导出**
  - [x] 交易记录导出 Excel (Apache POI SXSSFWorkbook 流式写入)
  - [x] 交易记录导出 CSV (UTF-8 BOM)
  - [x] 月度报表导出 (多工作表: 概要 + 分类 + 日趋势 + 明细)
  - [x] 年度报表导出 (12月趋势)
  - [x] 记账列表页添加导出下拉按钮 (Excel/CSV)
  - [x] 月度报表页添加导出按钮
  - [x] 健康评分页添加导出按钮
  - [x] 5 个后端单元测试

- [x] **仪表盘更新**
  - [x] 预算使用率卡片
  - [x] 风险画像卡片
  - [x] 侧边栏新增预算管理 + 投资建议菜单
  - [x] 路由新增 6 条 (预算3 + 投资3)

- [x] **前端单元测试**
  - [x] budget API 测试 (10 tests)
  - [x] investment API 测试 (7 tests)
  - [x] export API 测试 (5 tests)
  - [x] budget store 测试 (4 tests)
  - [x] investment store 测试 (5 tests)

## 待完成

### Phase 4 - AI Agent 改造 (规划中)

**目标**: 将 AI Chat 升级为完整 AI Agent，用户在聊天窗口即可操作系统所有功能。自研编排引擎，不使用开源 Agent 框架。

**设计决策**:
- LLM: 多模型切换 (DeepSeek/Claude/GPT)，均基于 OpenAI 兼容协议
- 确认机制: 高风险操作(删除、大额)需用户确认，读取和普通写入自动执行
- UI 策略: 保留现有页面，AI Chat 升级为 Agent 模式作为额外操作入口
- 架构: 服务端 ReAct 循环，SSE 流式推送全过程 (工具调用 + 结果 + 回复)

#### Phase 4.1 - 核心基础设施 + 记账/分析/预算工具

- [x] **Agent 工具体系**
  - [x] `Tool` 接口 + `AbstractTool` 基类 + `@AgentTool` 注解
  - [x] `RiskLevel` 枚举 (LOW/MEDIUM/HIGH)
  - [x] `ToolResult` 执行结果 DTO
  - [x] `ToolRegistry` 工具自动发现 + schema 生成
  - [x] `ToolExecutor` 参数校验 + 执行 + 错误处理

- [x] **增强 LLM Client (Function Calling)**
  - [x] `LlmClient` 新增 `streamChatWithTools()` 方法
  - [x] `OpenAiCompatibleLlmClient` 扩展: 请求体加入 tools/tool_choice 字段
  - [x] Streaming 响应解析: `choices[0].delta.tool_calls` 累积拼装
  - [x] `LlmStreamEvent` / `ToolCallChunk` DTO

- [x] **Agent 编排引擎 (ReAct Loop)**
  - [x] `AgentService` 接口 + `AgentServiceImpl`
  - [x] `AgentExecutionContext` 每请求状态(消息列表、迭代计数、确认状态)
  - [x] ReAct 循环: LLM 调用 → 解析响应 → 工具执行 → 结果注入 → 循环 (max 10 次)
  - [x] `AgentContextBuilder` (Agent 系统提示 + 工具 schema + 财务上下文 + 历史)
  - [x] `AgentSseHelper` 封装各类 SSE 事件发送

- [x] **确认流程**
  - [x] `ConfirmationStore` (ConcurrentHashMap + CompletableFuture)
  - [x] 高风险操作暂停循环 → SSE 推送确认请求 → 等待用户响应
  - [x] `POST /api/ai/agent/confirm` 确认/拒绝端点
  - [x] 动态风险评估(如金额 > 10000 升级为 HIGH)

- [x] **SSE 协议升级**
  - [x] 新增 SSE 事件: `tool_call_start`, `tool_call_result`, `confirmation_required`, `confirmation_resolved`, `done`
  - [x] 向后兼容: 默认事件 (无 event name) 保持现有格式
  - [x] `GET /api/ai/agent/chat-stream` 端点 (timeout 5min)

- [x] **数据库扩展**
  - [x] Flyway V11: `ai_conversation` 表新增 `message_type`, `tool_calls`, `tool_call_id` 列
  - [x] 上下文策略: 当前 turn 保留完整工具链，历史 turn 压缩为摘要

- [x] **Phase 1 工具实现 (15 个)**
  - [x] 记账: `list_transactions`, `get_recent_transactions`, `create_transaction`, `update_transaction`, `delete_transaction`, `get_categories`
  - [x] 分析: `get_monthly_summary`, `get_category_stats`, `get_health_score`
  - [x] 预算: `get_budget_summary`, `get_budgets`, `set_budget`, `delete_budget`, `get_budget_trend`, `get_budget_ai_suggestions`

- [x] **前端改造**
  - [x] `AgentStreamEvent` 等 TypeScript 类型定义
  - [x] `streamAgentChat()` API (增强 SSE 解析，处理 named events)
  - [x] `ToolCallCard.vue` 组件 (内联展示工具调用状态和结果)
  - [x] `ConfirmationDialog.vue` 组件 (内联确认，非模态弹窗)
  - [x] `ChatPage.vue` 改造: Agent/Chat 模式切换，渲染工具卡片和确认对话框
  - [x] `chat.ts` Store: `sendAgentMessage()` + `respondToConfirmation()`

#### Phase 4.2 - 投资/目标/副业工具

- [ ] **扩展工具 (13 个)**
  - [ ] 目标: `list_goals`, `create_goal`, `update_goal`, `delete_goal`, `add_goal_progress`, `generate_goal_ai_plan`
  - [ ] 投资: `get_risk_assessment`, `get_investment_advice`, `get_asset_allocation`
  - [ ] 副业: `get_career_recommendations`, `list_career_plans`, `create_career_plan`, `get_user_profile`
- [ ] `finance-planner-ai` pom.xml 添加 goal/investment/career 模块依赖
- [ ] 上下文压缩优化(token 计数 + 超限自动 compaction)
- [ ] 复杂多工具链场景测试

#### Phase 4.3 - 高级功能

- [ ] 多 Provider 健壮性(单 provider 失败自动降级)
- [ ] 工具结果 Redis 缓存 (同一会话内查询复用)
- [ ] Agent 分析仪表盘(工具使用频率、成功率、延迟)
- [ ] Rate limiting 适配 (Agent 一轮对话算 1 次)
- [ ] 错误恢复 (Agent 循环中断时状态清理)

### Phase 5 (规划中)

- [ ] 多语言支持
- [ ] 移动端适配
- [ ] 数据备份/恢复

## 技术架构总结

### 后端模块

| 模块 | 功能 |
|------|------|
| finance-planner-common | 公共工具、异常处理、响应封装 |
| finance-planner-auth | 用户认证、JWT、Spring Security |
| finance-planner-accounting | 分类管理、交易记录 CRUD |
| finance-planner-analysis | 统计分析、健康评分 |
| finance-planner-ai | AI 对话、多模型 LLM 集成、SSE 流式传输、OCR 导入 |
| finance-planner-goal | 理财目标规划、进度跟踪、AI 理财计划 |
| finance-planner-career | 副业推荐、用户资料、收入跟踪、AI 启动计划 |
| finance-planner-budget | 预算管理、预算对比、AI 优化建议 |
| finance-planner-investment | 风险评估、投资组合推荐、资产配置 |
| finance-planner-app | 主应用入口 |

### 健康评分算法

总分 100 分，5个维度：
- 储蓄能力 (30分): 月储蓄率与20%目标比较
- 收支平衡 (25分): 支出/收入比例
- 消费结构 (20分): 必需品消费占比
- 资产增长 (15分): 环比增长
- 记账习惯 (10分): 记录频率

### 前端页面

| 路径 | 页面 | 功能 |
|------|------|------|
| /login | 登录 | 用户认证 |
| /register | 注册 | 用户注册 |
| /dashboard | 仪表盘 | 首页概览 |
| /accounting | 记账列表 | 交易记录管理 |
| /accounting/new | 新建记录 | 记一笔 |
| /accounting/edit/:id | 编辑记录 | 修改交易 |
| /analysis/monthly | 月度报表 | 收支统计图表 |
| /analysis/health | 健康评分 | 财务健康分析 |
| /ai/chat | AI 顾问 | AI 对话、流式回复、模型切换 |
| /accounting/ocr | 拍照记账 | OCR 票据识别、自动记账 |
| /goals | 目标列表 | 理财目标管理、状态筛选 |
| /goals/new | 新建目标 | 创建理财目标 |
| /goals/:id | 目标详情 | 进度图表、存款记录、AI 建议 |
| /goals/edit/:id | 编辑目标 | 修改理财目标 |
| /career | AI推荐 | AI 副业推荐卡片 |
| /career/plans | 我的计划 | 副业计划列表 |
| /career/plans/:id | 计划详情 | 收入跟踪、AI 90天启动计划 |
| /career/profile | 个人资料 | 用户资料编辑 |
| /budget | 预算概览 | 预算 vs 实际对比、分类进度条 |
| /budget/edit | 设置预算 | 分类预算批量编辑 |
| /budget/trend | 预算趋势 | 近6月预算使用率趋势 |
| /investment | 投资建议 | 风险画像、资产配置饼图、推荐列表 |
| /investment/quiz | 风险评估 | 8题问卷、实时计分 |
| /investment/history | 评估历史 | 历次评估时间线 |

## 启动项目

```bash
# 启动 Docker 服务
docker-compose -f docker-compose-dev.yml up -d

# 启动后端
cd finance-planner-backend
mvn clean install
mvn spring-boot:run -pl finance-planner-app

# 启动前端
cd finance-planner-frontend
pnpm install
pnpm dev
```

## 关键文件路径

- 后端入口: `finance-planner-backend/finance-planner-app/`
- 前端入口: `finance-planner-frontend/`
- 数据库迁移: `finance-planner-backend/finance-planner-app/src/main/resources/db/migration/`
- API 配置: `finance-planner-backend/finance-planner-app/src/main/resources/application.yml`


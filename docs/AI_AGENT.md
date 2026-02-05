# AI 与 AI Agent 技术文档（UTF-8）

本文档描述 Come Rich 项目中 AI 能力与 AI Agent 的功能、架构设计、代码结构、调用流程与关键配置。内容基于现有代码实现，适用于后端与前端协作开发与排查问题。

## 1. 范围与目标

- AI 对话：基于 OpenAI 兼容协议的多模型对话，支持 SSE 流式输出与对话历史持久化。
- AI Agent：在 AI 对话基础上增加工具调用（Function Calling）与 ReAct 编排循环，实现“对话即操作”。
- OCR 票据识别：基于百度 OCR 完成票据识别与自动记账导入。

## 2. 功能概览

### 2.1 AI 对话（Chat）
- SSE 流式输出（后端 SseEmitter + 前端 fetch/ReadableStream 解析）。
- 多模型 LLM 切换（DeepSeek/Moonshot/Qwen 等，统一 OpenAI 兼容协议）。
- 财务上下文注入（统计与健康评分）。
- 对话历史持久化（ai_conversation 表）。
- Redis 限流（默认 10 次/天，可配置）。

### 2.2 AI Agent
- ReAct 循环：LLM 工具调用 → 工具执行 → 结果回注 → 继续推理。
- 工具体系：统一 Tool 接口、自动 schema 生成、参数校验与风险评估。
- 高风险确认：高风险操作需要用户确认（SSE 触发 + 前端确认）。
- SSE 事件升级：支持工具调用与确认等命名事件。

### 2.3 OCR 票据识别
- 图片上传 → OCR 识别 → 结构化信息抽取（金额、日期、商户）。
- 智能分类建议（关键词匹配）。
- 预览/确认/拒绝流程，确认后写入交易流水。

## 3. 架构设计

### 3.1 AI 对话架构

1. Controller 接收请求并创建 SseEmitter。
2. Service 校验限流 → 生成 sessionId → 记录用户消息。
3. PromptBuilder 注入系统提示 + 历史消息 + 财务上下文。
4. LlmClient 调用模型并按 SSE 流式返回。
5. Service 聚合流式内容并持久化最终回答。

核心类：
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/controller/AiChatController.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/service/impl/AiChatServiceImpl.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/service/impl/PromptBuilderImpl.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/service/impl/OpenAiCompatibleLlmClient.java`

### 3.2 AI Agent 架构

AI Agent 在对话基础上引入工具调用与 ReAct 循环。

关键流程：
1. 构建系统 Prompt + 财务上下文 + 历史对话。
2. 发送工具 schema（Function Calling）到 LLM。
3. LLM 返回内容与 tool_calls（可多次、分片）。
4. 汇总 tool_calls → 依次执行工具。
5. 工具结果以 tool 消息回注，继续下一轮推理。
6. 达到终止条件（finish_reason 或最大迭代）后结束。

核心组件：
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/service/AgentServiceImpl.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/service/AgentContextBuilder.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/tool/ToolRegistry.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/tool/ToolExecutor.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/service/ConfirmationStore.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/service/AgentSseHelper.java`

### 3.3 风险控制与确认

- 风险等级：LOW / MEDIUM / HIGH。
- 动态风险评估：工具可实现 `RiskAwareTool`，依据金额阈值动态提升风险。
- 风险阈值：默认 10000，可通过接口更新并存储在 Redis。
- 高风险执行前：发送 `confirmation_required`，等待用户确认（默认 300 秒超时）。

### 3.4 时间上下文与相对日期

- 系统提示会注入 `TimeContext`（日期与时区）。
- 相对日期解析：今天/昨天/明天/前天/后天（中英均支持）。
- 对于创建/更新交易工具，会优先使用用户输入中的相对日期覆盖模型输出。

相关类：
- `finance-planner-backend/finance-planner-common/src/main/java/com/finance/planner/ai/time/TimeContext.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/time/DefaultTimeContextProvider.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/util/RelativeDateResolver.java`
- `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/util/DateParser.java`

### 3.5 SSE 协议

AI Chat：默认事件，无 event name，data 为 JSON 字符串。

AI Agent：使用命名事件，事件类型如下：
- `message`
- `tool_call_start`
- `tool_call_result`
- `confirmation_required`
- `confirmation_resolved`
- `done`
- `error`

事件与前端类型约定：
- `finance-planner-frontend/src/types/ai.d.ts`
- `finance-planner-frontend/src/api/ai.ts`
- `finance-planner-frontend/src/stores/chat.ts`

## 4. 代码结构（AI 相关）

### 4.1 后端模块（finance-planner-ai）

```
finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai
├── agent/                      # Agent 编排与工具
│   ├── dto/                     # Agent SSE 与工具调用 DTO
│   ├── service/                 # AgentService、ContextBuilder、SSE Helper
│   ├── tool/                    # Tool 接口、schema、执行器
│   ├── tool/impl/               # 具体工具实现
│   ├── tool/params/             # 工具参数对象（带校验注解）
│   └── util/                    # 时间解析工具
├── config/                      # AI 配置（AiConfig、Provider）
├── controller/                  # AI Chat / Agent Controller
├── dto/                         # LLM stream DTO
├── entity/                      # ai_conversation 实体
├── ocr/                         # OCR 导入模块
│   ├── config/                  # Baidu OCR 配置
│   ├── controller/              # OCR API
│   ├── dto/                     # OCR DTO
│   ├── entity/                  # ocr_record 实体
│   ├── repository/              # OCR 持久化
│   └── service/                 # OCR 服务
├── repository/                  # AI 对话持久化
├── service/                     # AI Chat 与 LLM Provider
└── time/                        # TimeContext Provider
```

### 4.2 前端模块

```
finance-planner-frontend/src
├── api/ai.ts                    # AI Chat 与 Agent 请求
├── stores/chat.ts               # 聊天与 Agent 状态管理
├── types/ai.d.ts                # SSE 事件与消息类型
└── views/ai/ChatPage.vue         # AI Chat/Agent 页面
```

## 5. 工具清单（AI Agent）

目前实现的工具（@AgentTool 标注）：

| 工具名 | 风险 | 说明 |
|---|---|---|
| create_transaction | MEDIUM（金额超阈值可升为 HIGH） | 创建交易 |
| update_transaction | MEDIUM（金额超阈值可升为 HIGH） | 更新交易 |
| delete_transaction | HIGH | 删除交易 |
| list_transactions | LOW | 查询交易列表 |
| get_recent_transactions | LOW | 最近交易 |
| get_categories | LOW | 分类列表或树 |
| get_category_stats | LOW | 分类统计 |
| get_monthly_summary | LOW | 月度汇总 |
| get_health_score | LOW | 健康评分 |

参数模型位于 `finance-planner-backend/finance-planner-ai/src/main/java/com/finance/planner/ai/agent/tool/params/`，
并通过 `ToolSchemaGenerator` 生成 JSON Schema，字段规则来源于 Jakarta Validation 注解。

## 6. API 与调用入口

### 6.1 AI Chat
- `GET /api/ai/chat-stream`：SSE 流式聊天
- `GET /api/ai/history`：对话历史
- `GET /api/ai/remaining`：剩余次数
- `GET /api/ai/providers`：可用模型
- `GET /api/ai/provider`：当前模型
- `PUT /api/ai/provider`：切换模型

### 6.2 AI Agent
- `GET /api/ai/agent/chat-stream`：SSE 流式 Agent
- `POST /api/ai/agent/confirm`：高风险确认
- `GET /api/ai/agent/risk-threshold`：获取阈值
- `PUT /api/ai/agent/risk-threshold`：更新阈值

### 6.3 OCR
- `POST /api/accounting/ocr/upload`：上传图片并识别
- `POST /api/accounting/ocr/{id}/confirm`：确认并生成交易
- `POST /api/accounting/ocr/{id}/reject`：拒绝
- `GET /api/accounting/ocr/pending`：待处理列表

## 7. SSE 事件与载荷示例

AI Agent 命名事件示例（简化）：

```json
event: tool_call_start
data: {"toolCallId":"call_1","toolName":"list_transactions","arguments":"{\"page\":1}"}
```

```json
event: tool_call_result
data: {"toolCallId":"call_1","toolName":"list_transactions","result":{"success":true,"data":{}}}
```

```json
event: confirmation_required
data: {"confirmationId":"abc123","toolCallId":"call_2","toolName":"delete_transaction","riskLevel":"HIGH"}
```

```json
event: done
data: {"sessionId":"..."}
```

## 8. 数据持久化

### 8.1 AI 对话表：`ai_conversation`

- 记录用户与 AI 的多角色消息（user/assistant/tool）。
- 支持记录工具调用与工具结果。
- 关键字段：
  - `session_id`、`role`、`content`
  - `message_type`（text / tool_call / tool）
  - `tool_calls`（tool_calls JSON）
  - `tool_call_id`

### 8.2 OCR 表：`ocr_record`

- 记录 OCR 原始文本、抽取字段、状态与交易关联。
- 状态：1=pending, 2=confirmed, 3=rejected。

## 9. 调用流程（流程说明）

### 9.1 AI Chat

1. 前端 `streamChat()` 调用 `/api/ai/chat-stream`。
2. 后端 `AiChatServiceImpl` 校验限流并写入用户消息。
3. `PromptBuilderImpl` 生成系统提示与上下文。
4. `OpenAiCompatibleLlmClient` 调用 LLM，返回 SSE。
5. 前端解析 SSE，逐步拼接消息。

### 9.2 AI Agent

1. 前端 `streamAgentChat()` 调用 `/api/ai/agent/chat-stream`。
2. 后端 `AgentServiceImpl` 进入 ReAct 循环。
3. LLM 返回 tool_calls → `ToolExecutor` 执行工具。
4. 若风险为 HIGH，触发确认流程并等待用户响应。
5. 工具结果回注 → 继续推理或结束。

### 9.3 OCR 记账

1. 上传图片到 `/api/accounting/ocr/upload`。
2. OCR 识别并抽取金额/日期/商户/分类建议。
3. 用户确认后写入交易流水，并标记记录状态。

## 10. 关键配置

### 10.1 后端（.env）

- `AI_ACTIVE_PROVIDER`：当前模型供应商。
- `AI_DAILY_LIMIT`：每日对话次数限制。
- `DEEPSEEK_API_KEY` / `MOONSHOT_API_KEY` / `QWEN_API_KEY`：模型密钥。
- `BAIDU_OCR_API_KEY` / `BAIDU_OCR_SECRET_KEY`：OCR 密钥。

### 10.2 前端（.env.*）

- `VITE_FEATURE_AI_CHAT_ENABLED`
- `VITE_FEATURE_OCR_ENABLED`

详细配置请参见 `docs/ENV_CONFIGURATION.md`。

## 11. 关键注意事项

- Agent 工具 schema 基于参数类字段生成，请保持参数类与后端 DTO 一致。
- 高风险确认依赖 Redis/内存状态，超时将自动拒绝。
- 相对日期优先级：当用户输入包含相对日期时覆盖模型输出的日期字段。
- SSE 解析必须兼容命名事件与默认事件两种形式。

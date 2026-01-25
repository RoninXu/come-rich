# Come Rich - 个人理财规划师设计文档

## 项目概述

一个面向大众的综合性个人理财 Web 应用，后续计划开发移动 APP。

**核心功能：**
- 记账追踪 - 记录收入支出，了解钱花在哪里
- 预算管理 - 设定预算目标，控制支出
- AI 智能分析 - 消费洞察、预算建议、财务问答
- 投资管理 - 记录持仓，追踪收益
- 财务目标 - 设定存钱目标，追踪进度

## 技术架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Vue 3 前端     │────▶│  Spring Boot    │────▶│   PostgreSQL    │
│   (Vite 构建)    │◀────│  REST API       │◀────│   数据库         │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 前端技术选型

- Vue 3 + Composition API
- Vite 构建工具
- Vue Router 路由管理
- Pinia 状态管理
- Axios 请求库
- ECharts 图表展示
- Element Plus 或 Ant Design Vue 组件库

### 后端技术选型

- Spring Boot 3.x
- Spring Security + JWT 用户认证
- Spring Data JPA 数据访问
- Flyway 数据库版本管理
- Maven 构建

### 项目结构

```
come-rich/
├── come-rich-web/      # Vue 前端
├── come-rich-api/      # Spring Boot 后端
└── docs/               # 设计文档
```

## 数据模型

### 用户相关

```
User (用户)
├── id, email, password, nickname
├── avatar, created_at, updated_at
└── status (启用/禁用)
```

### 记账模块

```
Account (账户) - 银行卡、支付宝、现金等
├── id, user_id, name, type, balance
└── icon, color, is_default

Category (分类) - 餐饮、交通、工资等
├── id, user_id, name, type (收入/支出)
├── icon, color, parent_id (支持二级分类)
└── is_system (系统预设/用户自定义)

Transaction (交易记录)
├── id, user_id, account_id, category_id
├── type (收入/支出/转账)
├── amount, description, transaction_date
└── created_at, updated_at
```

### 预算模块

```
Budget (预算)
├── id, user_id, category_id (可选，不填为总预算)
├── amount, period (月度/年度)
└── year, month
```

### 投资模块

```
Portfolio (投资组合)
├── id, user_id, name
└── description, created_at

Holding (持仓)
├── id, portfolio_id, asset_type (股票/基金/债券/加密货币/其他)
├── asset_name, asset_code (可选，如股票代码)
├── quantity, avg_cost (平均成本)
├── current_price, current_value
└── last_updated

HoldingRecord (交易记录)
├── id, holding_id, type (买入/卖出/分红)
├── quantity, price, amount
└── fee, transaction_date
```

### 财务目标模块

```
Goal (目标)
├── id, user_id, name (如：买房首付、日本旅行)
├── target_amount, current_amount
├── deadline, icon, color
└── status (进行中/已完成/已放弃)

GoalContribution (目标存入记录)
├── id, goal_id, amount
├── note, contribution_date
└── created_at
```

## API 设计

### 认证接口

```
POST   /api/auth/register     # 注册
POST   /api/auth/login        # 登录，返回 JWT
POST   /api/auth/refresh      # 刷新 token
GET    /api/auth/me           # 获取当前用户信息
```

### 记账接口

```
# 账户管理
GET    /api/accounts          # 账户列表
POST   /api/accounts          # 创建账户
PUT    /api/accounts/{id}     # 更新账户
DELETE /api/accounts/{id}     # 删除账户

# 分类管理
GET    /api/categories        # 分类列表（含系统预设）
POST   /api/categories        # 创建自定义分类
PUT    /api/categories/{id}   # 更新分类
DELETE /api/categories/{id}   # 删除分类

# 交易记录
GET    /api/transactions      # 列表，支持筛选和分页
POST   /api/transactions      # 记一笔
PUT    /api/transactions/{id} # 修改
DELETE /api/transactions/{id} # 删除

# 统计
GET    /api/stats/overview    # 总览（本月收支、余额）
GET    /api/stats/trend       # 趋势（按日/周/月）
GET    /api/stats/by-category # 分类统计
```

### 预算接口

```
GET    /api/budgets           # 预算列表
POST   /api/budgets           # 设定预算
PUT    /api/budgets/{id}      # 修改预算
GET    /api/budgets/status    # 预算执行情况（已用/剩余）
```

### 投资接口

```
# 投资组合
GET    /api/portfolios
POST   /api/portfolios
PUT    /api/portfolios/{id}
DELETE /api/portfolios/{id}

# 持仓
GET    /api/portfolios/{id}/holdings
POST   /api/portfolios/{id}/holdings
PUT    /api/holdings/{id}
DELETE /api/holdings/{id}

# 交易记录
POST   /api/holdings/{id}/records
GET    /api/holdings/{id}/records
```

### 目标接口

```
GET    /api/goals
POST   /api/goals
PUT    /api/goals/{id}
DELETE /api/goals/{id}
POST   /api/goals/{id}/contributions
```

### AI 接口

```
POST   /api/ai/chat           # 财务问答
GET    /api/ai/insights       # 消费洞察
GET    /api/ai/budget-advice  # 预算建议
```

## 前端页面结构

### 公开页面（未登录）

```
/login           # 登录页
/register        # 注册页
/forgot-password # 忘记密码
```

### 主应用页面（需登录）

```
/dashboard       # 首页仪表盘
                  - 本月收支概览
                  - 账户余额汇总
                  - 最近交易记录
                  - 预算执行进度条

/transactions    # 记账流水
                  - 交易列表（支持筛选、搜索）
                  - 快速记账按钮
                  - 批量导入/导出

/stats           # 统计报表
                  - 收支趋势图（折线图）
                  - 分类占比（饼图）
                  - 月度/年度对比

/budgets         # 预算管理
                  - 预算设置
                  - 各分类预算使用情况

/ai              # AI 助手
                  - 对话式财务问答
                  - 消费洞察报告
                  - 预算建议

/investments     # 投资管理
                  - 持仓列表
                  - 收益统计
                  - 买卖记录

/goals           # 财务目标
                  - 目标卡片列表
                  - 进度可视化

/settings        # 设置
                  - 个人信息
                  - 账户管理
                  - 分类管理
                  - 数据导出
```

页面采用左侧导航栏 + 右侧内容区的经典布局，移动端自适应为底部 Tab 导航。

## 安全设计

### 用户认证

- 密码使用 BCrypt 加密存储
- JWT Token 认证，Access Token 有效期 2 小时
- Refresh Token 有效期 7 天，存储在 HttpOnly Cookie
- 登录失败 5 次后锁定账户 15 分钟

### 接口安全

- 所有 API 接口需要 JWT 认证（除登录注册）
- 用户只能访问自己的数据（SQL 查询强制带 user_id 条件）
- 请求频率限制，防止暴力破解
- HTTPS 强制（生产环境）

### 数据安全

- 敏感操作记录审计日志（登录、修改密码、删除数据）
- 定期数据库备份
- 金额字段使用 DECIMAL 类型，避免浮点数精度问题

### 输入校验

- 前端 + 后端双重校验
- 防止 SQL 注入（JPA 参数化查询）
- 防止 XSS（输入输出转义）

## AI 智能分析模块

### 架构设计

```
用户提问
    ↓
┌─────────────────┐
│  意图识别层     │  ← 规则匹配 + 关键词判断
└────────┬────────┘
         ↓
    ┌────┴────┐
    ↓         ↓
┌───────┐ ┌───────┐
│ 规则  │ │ 大模型 │
│ 引擎  │ │  API  │
└───────┘ └───────┘
    ↓         ↓
    └────┬────┘
         ↓
   组装回复返回用户
```

### 规则引擎处理（免费、快速）

- 简单查询："这个月花了多少钱"、"餐饮支出多少"
- 直接查数据库，格式化返回
- 预算剩余查询、账户余额查询

### 大模型处理（智能、有成本）

- 消费洞察分析："我的消费有什么问题"
- 预算建议："帮我制定下个月预算"
- 复杂问答："和上个月比，我哪里花多了"

### 成本控制策略

- 每用户每日 AI 分析次数限制（如 10 次）
- 结果缓存，相同问题复用答案
- 先用规则尝试，无法处理再调用大模型

### 数据安全

- 发送给大模型的数据做脱敏处理
- 不发送具体账户名称，只发送分类和金额

## 开发阶段

### 第一阶段：基础框架 + 记账核心

- 前后端项目初始化
- 用户注册、登录、JWT 认证
- 账户管理（增删改查）
- 分类管理（系统预设 + 自定义）
- 交易记录（记账、编辑、删除）
- Dashboard 基础统计

### 第二阶段：统计 + 预算 + AI 智能分析

- 收支统计图表（趋势、分类占比）
- 预算设置与执行跟踪
- 超支提醒
- AI 消费洞察（异常支出、省钱建议）
- AI 预算建议（根据历史数据推荐）
- AI 财务问答（对话式查询）

### 第三阶段：投资管理

- 投资组合管理
- 持仓记录与收益计算
- 投资统计图表

### 第四阶段：目标与完善

- 财务目标管理
- 数据导出（Excel/CSV）
- 移动端适配优化
- 性能优化

## 暂不实现（YAGNI）

- 银行 API 对接
- 社交分享功能
- 多币种支持
- 付费会员系统

以上功能后续根据用户反馈再考虑添加。

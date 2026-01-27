# AI个人理财规划师 - 技术实现文档

**文档版本**：v1.0  
**创建日期**：2026年1月  
**技术栈**：Spring Boot 3.x + Vue 3.4 + PostgreSQL + Redis + DeepSeek  
**作者**：Ronin

---

## 目录

1. [系统架构设计](#1-系统架构设计)
2. [开发环境搭建](#2-开发环境搭建)
3. [数据库设计](#3-数据库设计)
4. [后端实现](#4-后端实现)
5. [前端实现](#5-前端实现)
6. [AI能力实现](#6-ai能力实现)
7. [RESTful API设计](#7-restful-api设计)
8. [部署方案](#8-部署方案)
9. [开发规范](#9-开发规范)

---

## 1. 系统架构设计

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                          用户层（Client）                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐              ┌──────────────────┐        │
│  │   Web浏览器       │              │   移动APP         │        │
│  │   (Vue 3.4)      │              │   (uni-app)      │        │
│  └──────────────────┘              └──────────────────┘        │
│           │                                  │                  │
└───────────┼──────────────────────────────────┼──────────────────┘
            │                                  │
            │         HTTPS / WebSocket        │
            ▼                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                       负载均衡层（Nginx）                        │
├─────────────────────────────────────────────────────────────────┤
│  · 静态资源CDN加速                                               │
│  · SSL证书终结                                                   │
│  · 请求路由与负载均衡                                            │
└─────────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      应用服务层（Spring Boot）                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐   │
│  │  用户服务       │  │  记账服务       │  │  AI服务        │   │
│  │  · 认证/授权    │  │  · 手动记账     │  │  · 对话管理    │   │
│  │  · 用户管理     │  │  · OCR识别     │  │  · Prompt生成  │   │
│  │  · 权限控制     │  │  · 分类算法     │  │  · 意图识别    │   │
│  └────────────────┘  └────────────────┘  └────────────────┘   │
│                                                                  │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐   │
│  │  分析服务       │  │  副业服务       │  │  目标服务      │   │
│  │  · 收支统计     │  │  · 推荐引擎     │  │  · 目标管理    │   │
│  │  · 财务评分     │  │  · 匹配算法     │  │  · 进度跟踪    │   │
│  │  · 预测模型     │  │  · 计划生成     │  │  · 方案生成    │   │
│  └────────────────┘  └────────────────┘  └────────────────┘   │
│                                                                  │
└───────────┬────────────────────────┬─────────────────┬──────────┘
            │                        │                 │
            ▼                        ▼                 ▼
┌─────────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│   数据持久层         │  │   缓存层          │  │  外部服务         │
├─────────────────────┤  ├──────────────────┤  ├──────────────────┤
│                     │  │                  │  │                  │
│  PostgreSQL         │  │  Redis           │  │  DeepSeek API    │
│  ┌────────────────┐ │  │  ┌─────────────┐ │  │  ┌─────────────┐ │
│  │  用户表        │ │  │  │ Session     │ │  │  │ 大模型调用  │ │
│  │  记账表        │ │  │  │ 对话历史    │ │  │  └─────────────┘ │
│  │  目标表        │ │  │  │ Token限流   │ │  │                  │
│  │  Prompt表      │ │  │  │ 缓存QA     │ │  │  百度OCR API     │
│  │  ...           │ │  │  └─────────────┘ │  │  ┌─────────────┐ │
│  └────────────────┘ │  │                  │  │  │ 图像识别    │ │
│                     │  │                  │  │  └─────────────┘ │
└─────────────────────┘  └──────────────────┘  │                  │
                                               │  阿里云OSS       │
                                               │  ┌─────────────┐ │
                                               │  │ 对象存储    │ │
                                               │  │ (图片、文件)│ │
                                               │  └─────────────┘ │
                                               └──────────────────┘

监控与日志：
┌─────────────────────────────────────────────────────────────────┐
│  · Logback日志收集                                               │
│  · Spring Boot Actuator健康检查                                 │
│  · Prometheus指标监控（可选）                                    │
└─────────────────────────────────────────────────────────────────┘
```

**架构特点**：
- **前后端分离**：Vue前端 + Spring Boot后端，独立部署
- **微服务预留**：当前单体应用，未来可拆分为微服务
- **缓存优化**：Redis缓存热点数据，减少数据库压力
- **外部服务解耦**：AI、OCR通过接口调用，易于替换

### 1.2 技术栈总览

#### 后端技术栈

```yaml
核心框架:
  - Spring Boot: 3.2.x
  - Java: 17

Web层:
  - Spring MVC: RESTful API
  - Spring WebFlux: AI对话流式输出（SSE）
  - Spring Security: 认证授权
  - JWT: Token验证

数据层:
  - Spring Data JPA: ORM
  - PostgreSQL: 14.x
  - HikariCP: 数据库连接池（默认）
  - Flyway: 数据库版本管理

缓存与消息:
  - Spring Data Redis: Redis操作
  - Redis: 7.x（Session、缓存、限流）

工具库:
  - Lombok: 简化代码
  - MapStruct: 对象映射
  - Hutool: Java工具类库
  - Guava: Google工具库

API文档:
  - SpringDoc OpenAPI: Swagger UI集成

AI集成:
  - OkHttp3: HTTP客户端（调用DeepSeek）
  - Jackson: JSON序列化

OCR集成:
  - 百度AI SDK: OCR识别

日志:
  - Logback: 日志框架
  - SLF4J: 日志门面

构建工具:
  - Maven: 3.9.x
```

#### 前端技术栈

```yaml
核心框架:
  - Vue: 3.4.x
  - TypeScript: 5.x

构建工具:
  - Vite: 5.x（开发服务器、打包）
  - Node.js: 18.x LTS

UI框架:
  - Element Plus: 最新版
  - 图标: @element-plus/icons-vue

状态管理:
  - Pinia: 2.x（替代Vuex）

路由:
  - Vue Router: 4.x

HTTP客户端:
  - Axios: 1.x

工具库:
  - day.js: 日期处理
  - lodash-es: 工具函数
  - js-cookie: Cookie操作

数据可视化:
  - ECharts: 5.x（图表）

代码规范:
  - ESLint: 代码检查
  - Prettier: 代码格式化
  - TypeScript ESLint: TS规则

CSS预处理:
  - SCSS: CSS预处理器
```

#### 数据库

```yaml
关系数据库:
  - PostgreSQL: 14.x
  - 特性: JSON字段支持、全文搜索

NoSQL:
  - Redis: 7.x
  - 用途: Session、缓存、限流、对话历史
```

#### 外部服务

```yaml
AI服务:
  - DeepSeek: V3大模型
  - 价格: ¥0.14/百万tokens

OCR服务:
  - 百度OCR: 通用文字识别
  - 价格: ¥0.002/次

云服务（阿里云）:
  - ECS: 云服务器
  - OSS: 对象存储（图片）
  - RDS: 云数据库（生产环境）
  - Redis: 云缓存（生产环境）
```

#### 开发工具

```yaml
IDE:
  - IntelliJ IDEA: 后端开发
  - VS Code: 前端开发

版本控制:
  - Git: 代码版本管理
  - GitHub/GitLab: 代码托管

容器化:
  - Docker: 20.x
  - Docker Compose: 2.x

API测试:
  - Swagger UI: 自带
  - Postman: 手动测试
```

### 1.3 模块划分

#### 后端模块结构

```
finance-planner-backend/
├── finance-planner-common/          # 公共模块
│   ├── 通用工具类
│   ├── 常量定义
│   ├── 异常定义
│   └── 通用响应封装
│
├── finance-planner-auth/            # 认证授权模块
│   ├── 用户注册/登录
│   ├── JWT生成与验证
│   ├── 权限控制
│   └── Session管理
│
├── finance-planner-accounting/      # 记账模块
│   ├── 手动记账
│   ├── OCR识别
│   ├── 分类管理
│   └── 交易记录CRUD
│
├── finance-planner-ai/              # AI服务模块
│   ├── 对话管理
│   ├── Prompt管理
│   ├── 意图识别
│   ├── DeepSeek集成
│   └── 安全过滤
│
├── finance-planner-analysis/        # 数据分析模块
│   ├── 收支统计
│   ├── 财务评分
│   ├── 趋势预测
│   └── 报表生成
│
├── finance-planner-career/          # 副业推荐模块
│   ├── 副业库管理
│   ├── 匹配算法
│   ├── 计划生成
│   └── 进度跟踪
│
├── finance-planner-goal/            # 目标规划模块
│   ├── 目标管理
│   ├── 方案生成
│   ├── 进度跟踪
│   └── 达成分析
│
└── finance-planner-notification/    # 通知模块
    ├── 异常提醒
    ├── 目标进度提醒
    └── 系统通知
```

#### 前端模块结构

```
finance-planner-frontend/
├── src/
│   ├── views/                       # 页面
│   │   ├── auth/                   # 认证相关
│   │   │   ├── Login.vue
│   │   │   └── Register.vue
│   │   ├── accounting/             # 记账相关
│   │   │   ├── ManualRecord.vue
│   │   │   ├── PhotoRecord.vue
│   │   │   └── RecordList.vue
│   │   ├── analysis/               # 数据分析
│   │   │   ├── Dashboard.vue
│   │   │   ├── MonthlyReport.vue
│   │   │   └── TrendChart.vue
│   │   ├── ai/                     # AI对话
│   │   │   └── ChatPage.vue
│   │   ├── career/                 # 副业推荐
│   │   │   ├── Recommend.vue
│   │   │   └── Progress.vue
│   │   └── goal/                   # 目标规划
│   │       ├── GoalList.vue
│   │       └── GoalDetail.vue
│   │
│   ├── components/                  # 通用组件
│   │   ├── layout/                 # 布局组件
│   │   ├── form/                   # 表单组件
│   │   └── chart/                  # 图表组件
│   │
│   ├── stores/                      # Pinia状态管理
│   │   ├── user.ts                 # 用户状态
│   │   ├── accounting.ts           # 记账状态
│   │   └── ai.ts                   # AI对话状态
│   │
│   ├── router/                      # 路由配置
│   │   └── index.ts
│   │
│   ├── api/                         # API请求封装
│   │   ├── auth.ts
│   │   ├── accounting.ts
│   │   └── ai.ts
│   │
│   ├── utils/                       # 工具函数
│   │   ├── request.ts              # Axios封装
│   │   ├── auth.ts                 # Token管理
│   │   └── format.ts               # 格式化工具
│   │
│   └── types/                       # TypeScript类型定义
│       ├── user.ts
│       └── accounting.ts
```

---

### 1.4 核心流程时序图

#### 1.4.1 用户登录流程

```
┌──────┐        ┌──────────┐      ┌────────────┐      ┌──────────┐
│ 前端  │        │  Nginx   │      │ Spring Boot│      │PostgreSQL│
└──┬───┘        └────┬─────┘      └─────┬──────┘      └────┬─────┘
   │                 │                   │                   │
   │  POST /api/auth/login              │                   │
   ├────────────────>│                   │                   │
   │                 │   转发请求         │                   │
   │                 ├──────────────────>│                   │
   │                 │                   │  查询用户信息      │
   │                 │                   ├──────────────────>│
   │                 │                   │                   │
   │                 │                   │  返回用户数据      │
   │                 │                   │<──────────────────┤
   │                 │                   │                   │
   │                 │                   │  验证密码（BCrypt）│
   │                 │                   │                   │
   │                 │                   │  生成JWT Token    │
   │                 │                   │                   │
   │                 │   返回Token        │                   │
   │                 │<──────────────────┤                   │
   │  返回Token + 用户信息                │                   │
   │<────────────────┤                   │                   │
   │                 │                   │                   │
   │  存储Token到localStorage            │                   │
   │                 │                   │                   │
```

#### 1.4.2 拍照记账流程

```
┌──────┐   ┌──────────┐   ┌────────────┐   ┌──────────┐   ┌──────────┐
│ 前端  │   │  Nginx   │   │ Spring Boot│   │ 百度OCR  │   │PostgreSQL│
└──┬───┘   └────┬─────┘   └─────┬──────┘   └────┬─────┘   └────┬─────┘
   │             │                │                │              │
   │  1. 用户上传截图             │                │              │
   │  POST /api/accounting/ocr   │                │              │
   ├────────────>│                │                │              │
   │             │   转发请求     │                │              │
   │             ├───────────────>│                │              │
   │             │                │  2. 调用OCR API│              │
   │             │                ├───────────────>│              │
   │             │                │                │              │
   │             │                │  3. 返回识别结果│              │
   │             │                │<───────────────┤              │
   │             │                │  {              │              │
   │             │                │    amount: 50,  │              │
   │             │                │    merchant: "美团外卖"         │
   │             │                │  }              │              │
   │             │                │                │              │
   │             │                │  4. AI分类      │              │
   │             │                │  (美团外卖 → 餐饮-外卖)         │
   │             │                │                │              │
   │             │                │  5. 保存记录    │              │
   │             │                ├───────────────────────────────>│
   │             │                │                │              │
   │             │   返回记账记录  │                │              │
   │             │<───────────────┤                │              │
   │  显示识别结果│                │                │              │
   │<────────────┤                │                │              │
   │             │                │                │              │
   │  6. 用户确认或修改            │                │              │
   │  PUT /api/accounting/{id}    │                │              │
   ├────────────>│                │                │              │
   │             ├───────────────>│                │              │
   │             │                ├───────────────────────────────>│
   │             │                │                │              │
```

#### 1.4.3 AI对话流程（含上下文管理）

```
┌──────┐   ┌──────┐   ┌────────────┐   ┌────────┐   ┌──────────┐
│ 前端  │   │ Nginx│   │ Spring Boot│   │ Redis  │   │ DeepSeek │
└──┬───┘   └──┬───┘   └─────┬──────┘   └───┬────┘   └────┬─────┘
   │           │             │               │             │
   │  1. 用户输入："我该买基金吗？"           │             │
   │  POST /api/ai/chat      │               │             │
   ├──────────>│             │               │             │
   │           ├────────────>│               │             │
   │           │             │  2. 获取对话历史              │
   │           │             ├──────────────>│             │
   │           │             │  (最近5轮)     │             │
   │           │             │<──────────────┤             │
   │           │             │               │             │
   │           │             │  3. 获取用户财务数据          │
   │           │             │  (收入、支出、风险偏好)       │
   │           │             │               │             │
   │           │             │  4. 构建Prompt│             │
   │           │             │  (系统Prompt + 用户上下文)    │
   │           │             │               │             │
   │           │             │  5. 调用DeepSeek             │
   │           │             ├─────────────────────────────>│
   │           │             │               │             │
   │           │             │  6. 流式返回   │             │
   │           │   SSE流式推送│<─────────────────────────────┤
   │           │<────────────┤               │             │
   │  实时显示AI回复          │               │             │
   │<──────────┤             │               │             │
   │  (打字机效果)            │               │             │
   │           │             │  7. 保存对话记录              │
   │           │             ├──────────────>│             │
   │           │             │               │             │
   │           │             │  8. 添加免责声明              │
   │           │             │               │             │
```

#### 1.4.4 财务健康评分计算流程

```
┌──────┐        ┌────────────┐        ┌──────────┐
│ 前端  │        │ Spring Boot│        │PostgreSQL│
└──┬───┘        └─────┬──────┘        └────┬─────┘
   │                  │                     │
   │  GET /api/analysis/health-score       │
   ├─────────────────>│                     │
   │                  │  1. 查询最近3个月数据│
   │                  ├────────────────────>│
   │                  │  - 总收入           │
   │                  │  - 总支出           │
   │                  │  - 负债情况         │
   │                  │<────────────────────┤
   │                  │                     │
   │                  │  2. 计算各项指标     │
   │                  │                     │
   │                  │  储蓄能力:          │
   │                  │  = (月储蓄/月收入) × 30│
   │                  │                     │
   │                  │  收支平衡:          │
   │                  │  = (1-支出/收入) × 25│
   │                  │                     │
   │                  │  负债健康:          │
   │                  │  = 20 - (负债/收入)×20│
   │                  │                     │
   │                  │  3. 汇总得分        │
   │                  │  = 储蓄能力 + 收支平衡 + ...│
   │                  │                     │
   │  返回评分结果     │                     │
   │<─────────────────┤                     │
   │  {               │                     │
   │    totalScore: 78,│                    │
   │    breakdown: {  │                     │
   │      saving: 25, │                     │
   │      balance: 18,│                     │
   │      ...         │                     │
   │    }             │                     │
   │  }               │                     │
```

---

## 2. 开发环境搭建

### 2.1 环境要求

```yaml
操作系统: Windows 10/11 (64位)

后端开发:
  JDK: 17 (推荐使用Amazon Corretto或Oracle JDK)
  Maven: 3.9.x
  IDE: IntelliJ IDEA 2023.x+

前端开发:
  Node.js: 18.x LTS
  npm: 9.x 或 pnpm: 8.x (推荐)
  IDE: VS Code + Volar插件

数据库:
  PostgreSQL: 14.x
  Redis: 7.x

容器化:
  Docker Desktop: 4.x (用于Windows)
  Docker Compose: 2.x

其他工具:
  Git: 2.x
  Postman: 最新版 (API测试)
```

---

### 2.2 后端环境配置

#### 2.2.1 安装JDK 17

```bash
# 方式1: 使用SDKMAN（推荐，但需要WSL）
sdk install java 17.0.9-amzn

# 方式2: 手动下载安装
# 下载地址：https://www.oracle.com/java/technologies/downloads/#java17
# 或使用Amazon Corretto: https://aws.amazon.com/corretto/

# 验证安装
java -version
# 输出应包含 "openjdk version "17.x.x""
```

**配置环境变量**（Windows）：
```
JAVA_HOME = C:\Program Files\Java\jdk-17
Path += %JAVA_HOME%\bin
```

#### 2.2.2 安装Maven

```bash
# 下载地址：https://maven.apache.org/download.cgi
# 解压到：C:\Program Files\Maven\apache-maven-3.9.x

# 配置环境变量
MAVEN_HOME = C:\Program Files\Maven\apache-maven-3.9.x
Path += %MAVEN_HOME%\bin

# 验证安装
mvn -version
```

**配置Maven镜像**（加速依赖下载）：

编辑 `~/.m2/settings.xml`：
```xml
<settings>
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <name>Aliyun Maven</name>
            <url>https://maven.aliyun.com/repository/public</url>
            <mirrorOf>central</mirrorOf>
        </mirror>
    </mirrors>
</settings>
```

#### 2.2.3 安装PostgreSQL

```bash
# 下载地址：https://www.postgresql.org/download/windows/
# 安装过程中设置：
# - 端口：5432 (默认)
# - 超级用户密码：postgres (开发环境)
# - 字符集：UTF8

# 安装完成后，打开pgAdmin或命令行创建数据库
psql -U postgres

# 创建数据库
CREATE DATABASE finance_planner;

# 创建用户（可选，生产环境必须）
CREATE USER finance_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE finance_planner TO finance_user;
```

#### 2.2.4 安装Redis

```bash
# Windows没有官方Redis，使用Docker运行（推荐）
docker run -d --name redis -p 6379:6379 redis:7-alpine

# 或下载Windows移植版：
# https://github.com/tporadowski/redis/releases
```

#### 2.2.5 配置IntelliJ IDEA

**必装插件**：
- Lombok Plugin（自动生成getter/setter）
- JPA Buddy（可视化JPA实体设计）
- RestfulTool（快速测试API）

**项目配置**：
```
File -> Project Structure
- Project SDK: 17
- Language Level: SDK Default (17)

Settings -> Build, Execution, Deployment -> Compiler
- [x] Build project automatically
```

---

### 2.3 前端环境配置

#### 2.3.1 安装Node.js

```bash
# 下载地址：https://nodejs.org/
# 选择 18.x LTS版本

# 验证安装
node -v  # v18.x.x
npm -v   # 9.x.x
```

#### 2.3.2 安装pnpm（推荐，比npm更快）

```bash
npm install -g pnpm

# 验证
pnpm -v
```

#### 2.3.3 配置npm镜像

```bash
# 使用淘宝镜像加速
npm config set registry https://registry.npmmirror.com

# 或使用pnpm
pnpm config set registry https://registry.npmmirror.com
```

#### 2.3.4 配置VS Code

**必装插件**：
- Volar (Vue 3官方插件，替代Vetur)
- TypeScript Vue Plugin (Volar)
- ESLint
- Prettier - Code formatter
- Auto Rename Tag
- Path Intellisense

**settings.json配置**：
```json
{
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.formatOnSave": true,
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  },
  "[vue]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  },
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  }
}
```

---

### 2.4 数据库环境配置

#### 2.4.1 PostgreSQL配置

**创建开发数据库**：

```sql
-- 连接到PostgreSQL
psql -U postgres

-- 创建数据库
CREATE DATABASE finance_planner_dev
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'zh_CN.UTF-8'
    LC_CTYPE = 'zh_CN.UTF-8'
    TEMPLATE = template0;

-- 切换到新数据库
\c finance_planner_dev

-- 创建应用用户（可选）
CREATE USER finance_dev WITH PASSWORD 'dev123456';
GRANT ALL PRIVILEGES ON DATABASE finance_planner_dev TO finance_dev;
```

**配置连接参数**（application-dev.yml）：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finance_planner_dev
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
```

#### 2.4.2 Redis配置

**启动Redis**：
```bash
# Docker方式（推荐）
docker run -d \
  --name redis-dev \
  -p 6379:6379 \
  -v redis-data:/data \
  redis:7-alpine redis-server --appendonly yes

# 验证连接
docker exec -it redis-dev redis-cli
> PING
PONG
```

**Spring Boot配置**（application-dev.yml）：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:  # 开发环境无密码
      database: 0
      timeout: 5000ms
```

---

### 2.5 Docker开发环境

#### 2.5.1 安装Docker Desktop

```bash
# 下载地址：https://www.docker.com/products/docker-desktop/
# 安装后重启电脑

# 验证安装
docker --version
docker-compose --version
```

#### 2.5.2 创建docker-compose.yml（开发环境）

在项目根目录创建 `docker-compose-dev.yml`：

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    container_name: finance-postgres-dev
    environment:
      POSTGRES_DB: finance_planner_dev
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      TZ: Asia/Shanghai
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - finance-net

  redis:
    image: redis:7-alpine
    container_name: finance-redis-dev
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes
    networks:
      - finance-net

volumes:
  postgres-data:
  redis-data:

networks:
  finance-net:
    driver: bridge
```

**启动开发环境**：
```bash
# 启动所有服务
docker-compose -f docker-compose-dev.yml up -d

# 查看运行状态
docker-compose -f docker-compose-dev.yml ps

# 停止服务
docker-compose -f docker-compose-dev.yml down

# 停止并删除数据卷（清空数据）
docker-compose -f docker-compose-dev.yml down -v
```

---

### 2.6 项目初始化

#### 2.6.1 创建后端项目

```bash
# 方式1：使用Spring Initializr（推荐）
# 访问：https://start.spring.io/
# 配置：
# - Project: Maven
# - Language: Java
# - Spring Boot: 3.2.x
# - Packaging: Jar
# - Java: 17
# - Dependencies:
#   - Spring Web
#   - Spring Security
#   - Spring Data JPA
#   - PostgreSQL Driver
#   - Spring Data Redis
#   - Lombok
#   - Validation

# 方式2：使用IDEA自带的Spring Initializr
# File -> New -> Project -> Spring Initializr
```

**项目结构**：
```
finance-planner-backend/
├── src/
│   ├── main/
│   │   ├── java/com/finance/planner/
│   │   │   ├── FinancePlannerApplication.java
│   │   │   ├── config/           # 配置类
│   │   │   ├── controller/       # 控制器
│   │   │   ├── service/          # 服务层
│   │   │   ├── repository/       # 数据访问层
│   │   │   ├── entity/           # 实体类
│   │   │   ├── dto/              # 数据传输对象
│   │   │   ├── exception/        # 异常定义
│   │   │   └── util/             # 工具类
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
├── pom.xml
└── README.md
```

#### 2.6.2 创建前端项目

```bash
# 使用Vite创建Vue 3项目
pnpm create vite finance-planner-frontend --template vue-ts

cd finance-planner-frontend

# 安装依赖
pnpm install

# 安装Element Plus
pnpm add element-plus @element-plus/icons-vue

# 安装其他依赖
pnpm add axios vue-router@4 pinia dayjs lodash-es echarts

# 安装开发依赖
pnpm add -D @types/lodash-es sass
pnpm add -D eslint prettier eslint-plugin-vue
pnpm add -D @typescript-eslint/parser @typescript-eslint/eslint-plugin

# 启动开发服务器
pnpm dev
```

**项目结构**：
```
finance-planner-frontend/
├── public/
├── src/
│   ├── assets/              # 静态资源
│   ├── components/          # 通用组件
│   ├── views/               # 页面
│   ├── router/              # 路由
│   ├── stores/              # 状态管理
│   ├── api/                 # API接口
│   ├── utils/               # 工具函数
│   ├── types/               # TS类型定义
│   ├── App.vue
│   └── main.ts
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
└── README.md
```

---

### 2.7 验证环境

**后端验证**：
```bash
cd finance-planner-backend

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
mvn spring-boot:run

# 访问：http://localhost:8080
# 应看到Spring Boot默认欢迎页或错误页（因为还没配置路由）
```

**前端验证**：
```bash
cd finance-planner-frontend

# 启动开发服务器
pnpm dev

# 访问：http://localhost:5173
# 应看到Vite+Vue默认页面
```

**数据库验证**：
```bash
# PostgreSQL
psql -U postgres -d finance_planner_dev -c "SELECT version();"

# Redis
docker exec -it finance-redis-dev redis-cli PING
# 输出：PONG
```

**环境搭建完成！✅**

---

## 3. 数据库设计

### 3.1 ER图

```
┌─────────────────┐
│     User        │  用户表
│─────────────────│
│ id (PK)         │
│ username        │──────┐
│ password        │      │
│ email           │      │ 1
│ phone           │      │
│ created_at      │      │
│ updated_at      │      │
└─────────────────┘      │
                         │
                         │ N
                   ┌─────────────────┐
                   │  Transaction    │  交易记录表
                   │─────────────────│
                   │ id (PK)         │
                   │ user_id (FK)    │───┐
                   │ amount          │   │
                   │ type            │   │ 1
                   │ category_id (FK)│───┼──────────────┐
                   │ description     │   │              │
                   │ transaction_date│   │              │
                   │ created_at      │   │              │
                   └─────────────────┘   │              │
                                         │              │
                                         │ N            │ N
┌─────────────────┐                ┌─────────────────┐│
│   Category      │  分类表         │  AIConversation ││  AI对话表
│─────────────────│                │─────────────────││
│ id (PK)         │<───────────────│ id (PK)         ││
│ name            │                │ user_id (FK)    ││
│ parent_id       │                │ question        ││
│ icon            │                │ answer          ││
│ color           │                │ created_at      ││
│ type            │                └─────────────────┘│
└─────────────────┘                                   │
                                                      │
┌─────────────────┐                ┌─────────────────┐│
│   FinancialGoal │  理财目标表     │  CareerPlan     ││  副业计划表
│─────────────────│                │─────────────────││
│ id (PK)         │                │ id (PK)         ││
│ user_id (FK)    │<───────────────│ user_id (FK)    ││
│ title           │                │ career_type     ││
│ target_amount   │                │ status          ││
│ current_amount  │                │ start_date      ││
│ deadline        │                │ monthly_income  ││
│ status          │                │ created_at      ││
│ created_at      │                └─────────────────┘│
└─────────────────┘                                   │
         │                                            │
         │ 1                                          │
         │                                            │
         │ N                                          │
┌─────────────────┐                                   │
│   GoalProgress  │  目标进度表                        │
│─────────────────│                                   │
│ id (PK)         │                                   │
│ goal_id (FK)    │                                   │
│ amount          │                                   │
│ note            │                                   │
│ record_date     │                                   │
└─────────────────┘                                   │
                                                      │
┌─────────────────┐                                   │
│  PromptTemplate │  Prompt模板表                     │
│─────────────────│                                   │
│ id (PK)         │                                   │
│ name            │                                   │
│ type            │                                   │
│ content         │                                   │
│ version         │                                   │
│ is_active       │                                   │
│ created_at      │                                   │
└─────────────────┘                                   │
                                                      │
┌─────────────────┐                                   │
│  RiskProfile    │  风险偏好表                        │
│─────────────────│                                   │
│ id (PK)         │                                   │
│ user_id (FK)    │───────────────────────────────────┘
│ score           │
│ level           │
│ test_date       │
└─────────────────┘
```

---

### 3.2 表结构设计

#### 3.2.1 用户表（user）

```sql
CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,  -- BCrypt加密
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    nickname VARCHAR(50),
    avatar_url VARCHAR(255),
    status SMALLINT DEFAULT 1,  -- 1:正常 0:禁用
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

CREATE INDEX idx_user_username ON "user"(username);
CREATE INDEX idx_user_email ON "user"(email);

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON COLUMN "user".status IS '状态：1正常 0禁用';
```

#### 3.2.2 交易记录表（transaction）

```sql
CREATE TABLE transaction (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,  -- 金额，支持到分
    type SMALLINT NOT NULL,  -- 1:收入 2:支出
    category_id BIGINT NOT NULL,
    description VARCHAR(255),
    transaction_date DATE NOT NULL,  -- 交易日期
    payment_method VARCHAR(50),  -- 支付方式：现金/支付宝/微信/信用卡
    merchant VARCHAR(100),  -- 商家名称
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE INDEX idx_transaction_user_id ON transaction(user_id);
CREATE INDEX idx_transaction_date ON transaction(transaction_date);
CREATE INDEX idx_transaction_category ON transaction(category_id);
CREATE INDEX idx_transaction_type ON transaction(type);

COMMENT ON TABLE transaction IS '交易记录表';
COMMENT ON COLUMN transaction.type IS '交易类型：1收入 2支出';
```

#### 3.2.3 分类表（category）

```sql
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT,  -- 父分类ID，NULL表示一级分类
    type SMALLINT NOT NULL,  -- 1:收入 2:支出
    icon VARCHAR(50),  -- 图标名称
    color VARCHAR(20),  -- 颜色代码
    sort_order INT DEFAULT 0,
    is_system BOOLEAN DEFAULT TRUE,  -- 是否系统分类
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (parent_id) REFERENCES category(id)
);

CREATE INDEX idx_category_type ON category(type);
CREATE INDEX idx_category_parent ON category(parent_id);

COMMENT ON TABLE category IS '交易分类表';
COMMENT ON COLUMN category.is_system IS '系统分类不可删除';

-- 插入默认分类
INSERT INTO category (name, type, icon, color, is_system) VALUES
-- 支出类别
('餐饮', 2, 'food', '#FF6B6B', TRUE),
('交通', 2, 'car', '#4ECDC4', TRUE),
('居住', 2, 'home', '#95E1D3', TRUE),
('购物', 2, 'shopping', '#F38181', TRUE),
('娱乐', 2, 'game', '#AA96DA', TRUE),
('学习', 2, 'book', '#FCBAD3', TRUE),
('医疗', 2, 'health', '#A8D8EA', TRUE),
('人情', 2, 'gift', '#FFAAA7', TRUE),
-- 收入类别
('工资', 1, 'salary', '#66BB6A', TRUE),
('副业', 1, 'side-job', '#42A5F5', TRUE),
('投资', 1, 'invest', '#FFA726', TRUE),
('其他', 1, 'other', '#78909C', TRUE);

-- 插入二级分类（餐饮）
INSERT INTO category (name, parent_id, type, icon, is_system) 
SELECT '早餐', id, 2, 'breakfast', TRUE FROM category WHERE name='餐饮';
INSERT INTO category (name, parent_id, type, icon, is_system) 
SELECT '午餐', id, 2, 'lunch', TRUE FROM category WHERE name='餐饮';
INSERT INTO category (name, parent_id, type, icon, is_system) 
SELECT '晚餐', id, 2, 'dinner', TRUE FROM category WHERE name='餐饮';
INSERT INTO category (name, parent_id, type, icon, is_system) 
SELECT '外卖', id, 2, 'takeout', TRUE FROM category WHERE name='餐饮';
```

#### 3.2.4 AI对话记录表（ai_conversation）

```sql
CREATE TABLE ai_conversation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64) NOT NULL,  -- 对话会话ID
    role VARCHAR(20) NOT NULL,  -- user/assistant
    content TEXT NOT NULL,
    tokens INT,  -- Token消耗数
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_conversation_user ON ai_conversation(user_id);
CREATE INDEX idx_conversation_session ON ai_conversation(session_id);
CREATE INDEX idx_conversation_created ON ai_conversation(created_at);

COMMENT ON TABLE ai_conversation IS 'AI对话记录表';
COMMENT ON COLUMN ai_conversation.role IS '角色：user用户 assistant助手';
```

#### 3.2.5 理财目标表（financial_goal）

```sql
CREATE TABLE financial_goal (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    target_amount DECIMAL(12, 2) NOT NULL,
    current_amount DECIMAL(12, 2) DEFAULT 0,
    deadline DATE,
    status SMALLINT DEFAULT 1,  -- 1:进行中 2:已完成 3:已放弃
    priority SMALLINT DEFAULT 2,  -- 1:高 2:中 3:低
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_goal_user ON financial_goal(user_id);
CREATE INDEX idx_goal_status ON financial_goal(status);

COMMENT ON TABLE financial_goal IS '理财目标表';
COMMENT ON COLUMN financial_goal.status IS '状态：1进行中 2已完成 3已放弃';
```

#### 3.2.6 目标进度记录表（goal_progress）

```sql
CREATE TABLE goal_progress (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    note VARCHAR(255),
    record_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (goal_id) REFERENCES financial_goal(id) ON DELETE CASCADE
);

CREATE INDEX idx_progress_goal ON goal_progress(goal_id);
CREATE INDEX idx_progress_date ON goal_progress(record_date);

COMMENT ON TABLE goal_progress IS '目标进度记录表';
```

#### 3.2.7 副业计划表（career_plan）

```sql
CREATE TABLE career_plan (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    career_type VARCHAR(50) NOT NULL,  -- 副业类型
    title VARCHAR(100) NOT NULL,
    description TEXT,
    status SMALLINT DEFAULT 1,  -- 1:计划中 2:进行中 3:已完成 4:已放弃
    target_monthly_income DECIMAL(10, 2),
    actual_monthly_income DECIMAL(10, 2),
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_career_user ON career_plan(user_id);
CREATE INDEX idx_career_status ON career_plan(status);

COMMENT ON TABLE career_plan IS '副业计划表';
```

#### 3.2.8 风险偏好测评表（risk_profile）

```sql
CREATE TABLE risk_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    score INT NOT NULL,  -- 测评总分
    level VARCHAR(20) NOT NULL,  -- conservative/moderate/aggressive
    answers JSONB,  -- 测评答案（JSON格式）
    test_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_risk_user ON risk_profile(user_id);

COMMENT ON TABLE risk_profile IS '风险偏好测评表';
COMMENT ON COLUMN risk_profile.level IS '风险等级：conservative保守 moderate中等 aggressive激进';
```

#### 3.2.9 Prompt模板表（prompt_template）

```sql
CREATE TABLE prompt_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,  -- system/user/finance/career
    content TEXT NOT NULL,
    variables JSONB,  -- 可变参数定义
    version VARCHAR(20) DEFAULT '1.0',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_prompt_type ON prompt_template(type);
CREATE INDEX idx_prompt_active ON prompt_template(is_active);

COMMENT ON TABLE prompt_template IS 'AI Prompt模板表';

-- 插入系统Prompt
INSERT INTO prompt_template (name, type, content, is_active) VALUES
('系统基础Prompt', 'system', 
'你是一个专业的AI理财助手，但你不是投资顾问。

你的角色：
- 帮助用户理解财务数据
- 教育用户理财知识
- 提供方向性建议（而非具体产品推荐）

你的限制：
- 不推荐具体基金/股票名称
- 不承诺任何收益
- 不代替用户做投资决策
- 必须加上风险提示

你的风格：
- 友好、耐心、不说教
- 用简单语言解释复杂概念
- 多用emoji让对话更生动', 
TRUE);
```

---

### 3.3 索引设计

#### 3.3.1 查询优化索引

```sql
-- 交易记录表复合索引（用于统计查询）
CREATE INDEX idx_transaction_user_date ON transaction(user_id, transaction_date);
CREATE INDEX idx_transaction_user_type_date ON transaction(user_id, type, transaction_date);

-- AI对话表复合索引（用于获取最近对话）
CREATE INDEX idx_conversation_user_session_created 
ON ai_conversation(user_id, session_id, created_at DESC);

-- 目标表复合索引
CREATE INDEX idx_goal_user_status ON financial_goal(user_id, status);
```

#### 3.3.2 全文搜索索引（可选）

```sql
-- 为交易描述添加全文搜索
CREATE INDEX idx_transaction_description_fts 
ON transaction USING GIN(to_tsvector('simple', description));

-- 使用示例
-- SELECT * FROM transaction 
-- WHERE to_tsvector('simple', description) @@ to_tsquery('simple', '外卖');
```

---

### 3.4 数据字典

| 表名 | 中文名 | 行数估算（10万用户） | 说明 |
|------|--------|---------------------|------|
| user | 用户表 | 100,000 | 核心用户数据 |
| transaction | 交易记录表 | 36,000,000 | 假设每人每天1条记录 |
| category | 分类表 | 100 | 系统预定义分类 |
| ai_conversation | AI对话表 | 5,000,000 | 假设每人每天0.5次对话，保留30天 |
| financial_goal | 理财目标表 | 300,000 | 假设每人3个目标 |
| goal_progress | 目标进度表 | 3,600,000 | 每个目标平均12条记录 |
| career_plan | 副业计划表 | 100,000 | 假设10%用户有副业计划 |
| risk_profile | 风险偏好表 | 100,000 | 每人1条（最新） |
| prompt_template | Prompt模板表 | 20 | 运营配置 |

**存储空间估算**：
- transaction表：约10GB（3年数据）
- ai_conversation表：约2GB（保留30天）
- 其他表：<1GB
- **总计：约15GB（含索引约25GB）**

---

## 4. 后端实现

### 4.1 项目结构

```
finance-planner-backend/
├── src/main/java/com/finance/planner/
│   ├── FinancePlannerApplication.java
│   │
│   ├── config/                      # 配置类
│   │   ├── SecurityConfig.java      # Spring Security配置
│   │   ├── RedisConfig.java         # Redis配置
│   │   ├── WebConfig.java           # Web配置（CORS等）
│   │   └── SwaggerConfig.java       # Swagger配置
│   │
│   ├── controller/                  # 控制器层
│   │   ├── AuthController.java      # 认证相关
│   │   ├── TransactionController.java
│   │   ├── AIController.java
│   │   ├── AnalysisController.java
│   │   ├── GoalController.java
│   │   └── CareerController.java
│   │
│   ├── service/                     # 服务层
│   │   ├── impl/
│   │   ├── UserService.java
│   │   ├── TransactionService.java
│   │   ├── AIService.java
│   │   ├── OCRService.java
│   │   ├── AnalysisService.java
│   │   └── CareerService.java
│   │
│   ├── repository/                  # 数据访问层
│   │   ├── UserRepository.java
│   │   ├── TransactionRepository.java
│   │   ├── CategoryRepository.java
│   │   └── ...
│   │
│   ├── entity/                      # 实体类
│   │   ├── User.java
│   │   ├── Transaction.java
│   │   └── ...
│   │
│   ├── dto/                         # 数据传输对象
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── TransactionRequest.java
│   │   │   └── ChatRequest.java
│   │   └── response/
│   │       ├── UserResponse.java
│   │       ├── TransactionResponse.java
│   │       └── ApiResponse.java     # 通用响应封装
│   │
│   ├── exception/                   # 异常定义
│   │   ├── BusinessException.java
│   │   ├── UnauthorizedException.java
│   │   └── GlobalExceptionHandler.java
│   │
│   ├── security/                    # 安全相关
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserDetailsServiceImpl.java
│   │
│   ├── util/                        # 工具类
│   │   ├── DateUtil.java
│   │   ├── EncryptUtil.java
│   │   └── ValidationUtil.java
│   │
│   └── constant/                    # 常量定义
│       ├── ErrorCode.java
│       └── BusinessConstant.java
│
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    ├── application-prod.yml
    └── db/migration/                # Flyway迁移脚本
        ├── V1__init_schema.sql
        └── V2__add_prompt_table.sql
```

---

### 4.2 核心模块实现

#### 4.2.1 用户认证模块

**JwtTokenProvider.java**（伪代码）：
```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    // 生成Token
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("username", username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    // 验证Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // 从Token获取用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
```

**AuthController.java**（伪代码）：
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        // 1. 验证用户名密码
        User user = userService.authenticate(
            request.getUsername(), 
            request.getPassword()
        );
        
        // 2. 生成Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        
        // 3. 返回响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(UserResponse.from(user));
        
        return ApiResponse.success(response);
    }
    
    @PostMapping("/register")
    public ApiResponse<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success();
    }
}
```

---

#### 4.2.2 记账模块

**TransactionService.java**（核心逻辑）：
```java
@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private CategoryService categoryService;
    
    // 手动记账
    public Transaction createTransaction(TransactionRequest request, Long userId) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategoryId(request.getCategoryId());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        
        return transactionRepository.save(transaction);
    }
    
    // 获取月度统计
    public MonthlyStatistics getMonthlyStatistics(Long userId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // 查询该月所有交易
        List<Transaction> transactions = transactionRepository
            .findByUserIdAndTransactionDateBetween(userId, startDate, endDate);
        
        // 统计收入/支出
        BigDecimal totalIncome = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal totalExpense = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 按分类统计
        Map<Long, BigDecimal> expenseByCategory = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .collect(Collectors.groupingBy(
                Transaction::getCategoryId,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ));
        
        return MonthlyStatistics.builder()
            .totalIncome(totalIncome)
            .totalExpense(totalExpense)
            .balance(totalIncome.subtract(totalExpense))
            .expenseByCategory(expenseByCategory)
            .build();
    }
}
```

---

#### 4.2.3 AI对话模块

**AIService.java**（核心逻辑）：
```java
@Service
public class AIService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private AIConversationRepository conversationRepository;
    
    @Autowired
    private PromptTemplateService promptTemplateService;
    
    @Autowired
    private UserService userService;
    
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.url}")
    private String apiUrl;
    
    // 流式对话（SSE）
    public Flux<String> chatStream(String userMessage, Long userId, String sessionId) {
        // 1. 获取对话历史（最近5轮）
        List<AIConversation> history = getRecentHistory(userId, sessionId, 5);
        
        // 2. 获取用户财务上下文
        UserFinancialContext context = userService.getFinancialContext(userId);
        
        // 3. 构建Prompt
        String systemPrompt = promptTemplateService.getSystemPrompt();
        String userPrompt = buildUserPrompt(userMessage, context, history);
        
        // 4. 调用DeepSeek（流式）
        return callDeepSeekStream(systemPrompt, userPrompt)
            .doOnNext(chunk -> {
                // 5. 保存对话记录
                saveConversation(userId, sessionId, "user", userMessage);
            })
            .doOnComplete(() -> {
                // 6. 保存完整AI回复
                saveConversation(userId, sessionId, "assistant", fullResponse);
            });
    }
    
    // 调用DeepSeek API（伪代码）
    private Flux<String> callDeepSeekStream(String systemPrompt, String userPrompt) {
        // 使用WebClient调用DeepSeek API
        // 返回流式数据
        return webClient.post()
            .uri(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(buildRequestBody(systemPrompt, userPrompt))
            .retrieve()
            .bodyToFlux(String.class)
            .map(this::parseStreamChunk);
    }
    
    // 构建用户Prompt
    private String buildUserPrompt(String message, UserFinancialContext context, 
                                   List<AIConversation> history) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加用户上下文
        prompt.append("用户财务数据：\n");
        prompt.append("- 月收入：¥").append(context.getMonthlyIncome()).append("\n");
        prompt.append("- 月支出：¥").append(context.getMonthlyExpense()).append("\n");
        prompt.append("- 风险偏好：").append(context.getRiskLevel()).append("\n\n");
        
        // 添加对话历史
        if (!history.isEmpty()) {
            prompt.append("对话历史：\n");
            history.forEach(conv -> {
                prompt.append(conv.getRole()).append(": ").append(conv.getContent()).append("\n");
            });
            prompt.append("\n");
        }
        
        // 添加当前问题
        prompt.append("用户问题：").append(message);
        
        return prompt.toString();
    }
}
```

---

#### 4.2.4 OCR识别模块

**OCRService.java**（伪代码）：
```java
@Service
public class OCRService {
    @Autowired
    private AipOcr ocrClient;  // 百度OCR客户端
    
    @Autowired
    private CategoryService categoryService;
    
    // 识别账单
    public TransactionOCRResult recognizeReceipt(MultipartFile image) throws IOException {
        // 1. 调用百度OCR
        byte[] imageBytes = image.getBytes();
        JSONObject result = ocrClient.generalBasic(imageBytes, new HashMap<>());
        
        // 2. 解析识别结果
        String fullText = extractText(result);
        
        // 3. 提取金额
        BigDecimal amount = extractAmount(fullText);
        
        // 4. 提取商家名称
        String merchant = extractMerchant(fullText);
        
        // 5. AI智能分类
        Long categoryId = classifyByMerchant(merchant);
        
        return TransactionOCRResult.builder()
            .amount(amount)
            .merchant(merchant)
            .categoryId(categoryId)
            .rawText(fullText)
            .build();
    }
    
    // AI分类逻辑
    private Long classifyByMerchant(String merchant) {
        if (merchant.contains("美团") || merchant.contains("饿了么")) {
            return categoryService.getCategoryIdByName("餐饮-外卖");
        } else if (merchant.contains("滴滴") || merchant.contains("出租车")) {
            return categoryService.getCategoryIdByName("交通-打车");
        }
        // ... 更多规则
        return categoryService.getDefaultCategoryId();
    }
    
    // 提取金额（正则表达式）
    private BigDecimal extractAmount(String text) {
        Pattern pattern = Pattern.compile("¥?(\\d+\\.\\d{2})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return new BigDecimal(matcher.group(1));
        }
        throw new BusinessException("无法识别金额");
    }
}
```

---

#### 4.2.5 副业推荐模块

**CareerService.java**（匹配算法伪代码）：
```java
@Service
public class CareerService {
    @Autowired
    private UserService userService;
    
    // 推荐副业
    public List<CareerRecommendation> recommendCareers(Long userId) {
        // 1. 获取用户画像
        UserProfile profile = userService.getUserProfile(userId);
        
        // 2. 加载副业库
        List<CareerOption> careerOptions = loadCareerOptions();
        
        // 3. 计算匹配度
        List<CareerRecommendation> recommendations = careerOptions.stream()
            .map(career -> {
                int score = calculateMatchScore(profile, career);
                return new CareerRecommendation(career, score);
            })
            .filter(rec -> rec.getScore() > 50)  // 过滤低匹配度
            .sorted(Comparator.comparingInt(CareerRecommendation::getScore).reversed())
            .limit(5)
            .collect(Collectors.toList());
        
        return recommendations;
    }
    
    // 计算匹配度（简化版）
    private int calculateMatchScore(UserProfile profile, CareerOption career) {
        int score = 0;
        
        // 职业匹配（40分）
        if (career.getRequiredSkills().contains(profile.getOccupation())) {
            score += 40;
        }
        
        // 时间投入匹配（30分）
        if (career.getTimeRequired() <= profile.getAvailableTime()) {
            score += 30;
        }
        
        // 收入期望匹配（30分）
        if (career.getExpectedIncome() >= profile.getIncomeExpectation()) {
            score += 30;
        }
        
        return score;
    }
}
```

---

#### 4.2.6 目标规划模块

**GoalService.java**（方案生成伪代码）：
```java
@Service
public class GoalService {
    @Autowired
    private TransactionService transactionService;
    
    // 生成目标达成方案
    public GoalAchievementPlan generatePlan(FinancialGoal goal, Long userId) {
        // 1. 计算缺口
        BigDecimal gap = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        
        // 2. 计算剩余月数
        long monthsLeft = ChronoUnit.MONTHS.between(
            LocalDate.now(), 
            goal.getDeadline().toLocalDate()
        );
        
        // 3. 计算每月需要存多少
        BigDecimal monthlyRequired = gap.divide(
            BigDecimal.valueOf(monthsLeft), 
            2, 
            RoundingMode.HALF_UP
        );
        
        // 4. 获取当前储蓄情况
        MonthlyStatistics currentStats = transactionService.getCurrentMonthStats(userId);
        BigDecimal currentMonthlySaving = currentStats.getBalance();
        
        // 5. 计算差距
        BigDecimal savingGap = monthlyRequired.subtract(currentMonthlySaving);
        
        // 6. 生成方案
        GoalAchievementPlan plan = new GoalAchievementPlan();
        plan.setMonthlyRequired(monthlyRequired);
        plan.setCurrentMonthlySaving(currentMonthlySaving);
        plan.setSavingGap(savingGap);
        
        // 7. 生成具体建议
        if (savingGap.compareTo(BigDecimal.ZERO) > 0) {
            plan.setSuggestions(generateSuggestions(savingGap, currentStats));
        } else {
            plan.setAchievable(true);
        }
        
        return plan;
    }
    
    // 生成具体建议
    private List<String> generateSuggestions(BigDecimal gap, MonthlyStatistics stats) {
        List<String> suggestions = new ArrayList<>();
        
        // 分析支出结构，找出可优化项
        if (stats.getExpenseByCategory().get("餐饮-外卖") > threshold) {
            suggestions.add("减少外卖支出可省 ¥XXX/月");
        }
        
        suggestions.add("开展副业增加收入 ¥XXX/月");
        
        return suggestions;
    }
}
```

---

### 4.3 通用组件

#### 4.3.1 统一响应封装

```java
@Data
@Builder
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .code(200)
            .message("success")
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
```

---

### 4.4 异常处理

**GlobalExceptionHandler.java**：
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<?> handleUnauthorizedException(UnauthorizedException e) {
        return ApiResponse.error(401, "未授权");
    }
    
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error(500, "系统异常");
    }
}
```

---

## 5. 前端实现

### 5.1 项目结构

[待填充]

### 5.2 核心页面实现

#### 5.2.1 登录注册页

[待填充]

#### 5.2.2 记账页面

[待填充]

#### 5.2.3 AI对话页面

[待填充]

#### 5.2.4 数据报表页面

[待填充]

### 5.3 状态管理（Pinia）

[待填充]

### 5.4 路由设计

[待填充]

### 5.5 通用组件库

[待填充]

---

## 6. AI能力实现

### 6.1 Prompt工程

[待填充]

### 6.2 对话上下文管理

[待填充]

### 6.3 意图识别

[待填充]

### 6.4 安全过滤

[待填充]

---

## 7. RESTful API设计

### 7.1 API设计规范

[待填充]

### 7.2 认证与授权

[待填充]

### 7.3 核心接口文档

#### 7.3.1 用户模块API

[待填充]

#### 7.3.2 记账模块API

[待填充]

#### 7.3.3 AI对话模块API

[待填充]

#### 7.3.4 统计分析模块API

[待填充]

### 7.4 错误码定义

[待填充]

### 7.5 前后端对接规范

[待填充]

---

## 8. 部署方案

### 8.1 Docker容器化

[待填充]

### 8.2 Docker Compose编排

[待填充]

### 8.3 Nginx配置

[待填充]

### 8.4 阿里云部署指南

[待填充]

---

## 9. 开发规范

### 9.1 Git工作流

[待填充]

### 9.2 代码规范

[待填充]

### 9.3 命名规范

[待填充]

### 9.4 注释规范

[待填充]

---

## 附录

### A. 常见问题FAQ

[待填充]

### B. 技术决策记录（ADR）

[待填充]

### C. 第三方依赖清单

[待填充]

## 5. 前端实现（精简版）

### 5.1 项目结构

已在第2章说明，核心采用Vue 3 + TypeScript + Element Plus + Pinia

### 5.2 状态管理（Pinia）

```typescript
// stores/user.ts
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as UserInfo | null
  }),
  actions: {
    async login(username: string, password: string) {
      const res = await authApi.login({ username, password })
      this.token = res.data.token
      this.userInfo = res.data.user
      localStorage.setItem('token', this.token)
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  }
})
```

### 5.3 路由设计

```typescript
// router/index.ts
const routes = [
  { path: '/login', component: () => import('@/views/auth/Login.vue') },
  { 
    path: '/', 
    component: Layout,
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'accounting', component: Accounting },
      { path: 'ai-chat', component: AIChat },
      { path: 'analysis', component: Analysis },
      { path: 'goal', component: Goal }
    ]
  }
]

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})
```

### 5.4 核心页面示例

**AI对话页面（伪代码）**：
```vue
<template>
  <div class="chat-container">
    <div class="message-list">
      <div v-for="msg in messages" :key="msg.id" :class="msg.role">
        <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
        <div class="content" v-html="markdownToHtml(msg.content)"></div>
      </div>
    </div>
    <div class="input-area">
      <el-input v-model="userInput" @keyup.enter="sendMessage" />
      <el-button @click="sendMessage">发送</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { aiApi } from '@/api'

const messages = ref<Message[]>([])
const userInput = ref('')

const sendMessage = async () => {
  const question = userInput.value
  messages.value.push({ role: 'user', content: question })
  
  // 调用SSE流式接口
  const eventSource = new EventSource(`/api/ai/chat?message=${question}`)
  let aiResponse = ''
  
  eventSource.onmessage = (event) => {
    aiResponse += event.data
    // 实时更新AI回复
    messages.value[messages.value.length - 1].content = aiResponse
  }
  
  eventSource.onerror = () => {
    eventSource.close()
  }
  
  userInput.value = ''
}
</script>
```

---

## 6. AI能力实现

### 6.1 Prompt工程

**Prompt存储在数据库（prompt_template表）**：

```yaml
系统Prompt（type: system）:
  name: 基础系统Prompt
  content: |
    你是专业的AI理财助手，遵循以下原则：
    - 不推荐具体基金/股票
    - 不承诺收益
    - 回复末尾添加风险提示
    - 使用emoji增加亲和力

用户上下文Prompt（type: user_context）:
  name: 用户财务上下文
  content: |
    用户财务数据：
    - 月收入：{{monthly_income}}
    - 月支出：{{monthly_expense}}
    - 风险偏好：{{risk_level}}
```

### 6.2 对话上下文管理

**Redis存储结构**：
```
Key: chat:history:{userId}:{sessionId}
Value: JSON数组，最多保留5轮对话
TTL: 30分钟（无活动自动过期）

示例：
[
  {"role": "user", "content": "我该买基金吗？"},
  {"role": "assistant", "content": "让我先了解..."},
  ...
]
```

### 6.3 意图识别

```java
public enum IntentType {
    GENERAL_FINANCE,    // 通用理财咨询
    INVESTMENT_ADVICE,  // 投资建议
    CAREER_PLANNING,    // 副业规划
    GOAL_SETTING,       // 目标设定
    EXPENSE_ANALYSIS    // 支出分析
}

// 简单规则匹配（MVP阶段）
public IntentType detectIntent(String message) {
    if (message.contains("副业") || message.contains("赚钱")) {
        return CAREER_PLANNING;
    } else if (message.contains("基金") || message.contains("股票")) {
        return INVESTMENT_ADVICE;
    }
    return GENERAL_FINANCE;
}
```

---

## 7. RESTful API设计

### 7.1 API设计规范

**命名规范**：
- 使用复数名词：`/api/transactions`（不是`/api/transaction`）
- 使用kebab-case：`/api/ai-conversations`
- 版本控制：`/api/v1/...`（后续扩展）

**HTTP方法**：
- GET：查询
- POST：创建
- PUT：更新（全量）
- PATCH：更新（部分）
- DELETE：删除

### 7.2 核心接口文档

#### 7.2.1 用户认证API

```yaml
POST /api/auth/login
Description: 用户登录
Request Body:
  {
    "username": "string",
    "password": "string"
  }
Response (200):
  {
    "code": 200,
    "message": "success",
    "data": {
      "token": "jwt_token_string",
      "user": {
        "id": 1,
        "username": "user123",
        "email": "user@example.com"
      }
    }
  }
```

#### 7.2.2 记账API

```yaml
POST /api/transactions
Description: 创建交易记录
Headers:
  Authorization: Bearer {token}
Request Body:
  {
    "amount": 50.00,
    "type": 2,  // 1:收入 2:支出
    "categoryId": 3,
    "description": "午餐",
    "transactionDate": "2026-01-26",
    "paymentMethod": "支付宝"
  }
Response (200):
  {
    "code": 200,
    "data": {
      "id": 123,
      "amount": 50.00,
      "category": "餐饮-午餐",
      ...
    }
  }
```

#### 7.2.3 AI对话API（SSE流式）

```yaml
GET /api/ai/chat-stream
Description: AI流式对话（Server-Sent Events）
Headers:
  Authorization: Bearer {token}
Query Parameters:
  message: "我该买基金吗？"
  sessionId: "uuid"
Response (Content-Type: text/event-stream):
  data: {"chunk": "基"}
  data: {"chunk": "于"}
  data: {"chunk": "你"}
  ...
  data: {"done": true}
```

#### 7.2.4 OCR识别API

```yaml
POST /api/accounting/ocr
Description: OCR识别账单
Headers:
  Authorization: Bearer {token}
Content-Type: multipart/form-data
Request:
  image: (file)
Response (200):
  {
    "code": 200,
    "data": {
      "amount": 50.00,
      "merchant": "美团外卖",
      "categoryId": 3,
      "rawText": "..."
    }
  }
```

### 7.3 错误码定义

```java
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(400, "业务异常"),
    SYSTEM_ERROR(500, "系统异常"),
    
    // 业务错误码
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    INSUFFICIENT_BALANCE(2001, "余额不足"),
    OCR_FAILED(3001, "图片识别失败");
}
```

### 7.4 前后端对接规范

**统一响应格式**：
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1706227200000
}
```

**分页格式**：
```json
{
  "code": 200,
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

---

## 8. 部署方案

### 8.1 Docker容器化

**后端Dockerfile**：
```dockerfile
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY target/finance-planner.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**前端Dockerfile**：
```dockerfile
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

### 8.2 Docker Compose编排

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: finance_planner
      POSTGRES_USER: finance
      POSTGRES_PASSWORD: your_password
    volumes:
      - postgres-data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    volumes:
      - redis-data:/data

  backend:
    build: ./backend
    depends_on:
      - postgres
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/finance_planner
      SPRING_REDIS_HOST: redis
      DEEPSEEK_API_KEY: ${DEEPSEEK_API_KEY}
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    depends_on:
      - backend
    ports:
      - "80:80"

volumes:
  postgres-data:
  redis-data:
```

### 8.3 Nginx配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://backend:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # SSE长连接配置
    location /api/ai/chat-stream {
        proxy_pass http://backend:8080/api/ai/chat-stream;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_buffering off;
    }
}
```

### 8.4 阿里云部署指南

**资源配置建议（MVP）**：
- ECS：2核4G（约¥100/月）
- RDS PostgreSQL：2核4G（约¥150/月）
- Redis：1G（约¥50/月）
- OSS：按量计费（约¥30/月）
- 总计：约¥330/月

**部署步骤**：
1. 购买ECS，安装Docker
2. 配置安全组（开放80、443、8080端口）
3. 上传代码，执行`docker-compose up -d`
4. 配置域名解析
5. 申请SSL证书（Let's Encrypt免费）

---

## 9. 开发规范

### 9.1 Git工作流

```
main (生产分支)
  ↑
develop (开发分支)
  ↑
feature/xxx (功能分支)
```

**分支命名**：
- feature/add-ocr：新功能
- bugfix/fix-login：修复Bug
- hotfix/urgent-fix：紧急修复

**提交规范**：
```
feat: 添加OCR识别功能
fix: 修复登录Token过期问题
docs: 更新API文档
refactor: 重构AI对话逻辑
```

### 9.2 代码规范

**Java**：
- 遵循阿里巴巴Java开发手册
- 使用Lombok减少样板代码
- 必须写注释（方法、类、复杂逻辑）

**TypeScript/Vue**：
- 使用ESLint + Prettier
- 组件命名：PascalCase
- 文件命名：kebab-case

### 9.3 命名规范

**数据库**：
- 表名：小写+下划线（transaction）
- 字段：小写+下划线（user_id）
- 索引：idx_表名_字段（idx_transaction_user_id）

**Java**：
- 类名：PascalCase（UserService）
- 方法名：camelCase（getUserById）
- 常量：UPPER_SNAKE_CASE（MAX_RETRY_COUNT）

**前端**：
- 组件：PascalCase（UserProfile.vue）
- 变量：camelCase（userInfo）
- 常量：UPPER_SNAKE_CASE（API_BASE_URL）

---

## 10. 安全设计

### 10.1 传输安全

```
HTTPS配置：
├─ 全站强制HTTPS
├─ TLS 1.3协议
├─ HSTS（HTTP Strict Transport Security）
└─ 证书固定（移动端）

Nginx配置示例：
server {
    listen 443 ssl http2;
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
    add_header Strict-Transport-Security "max-age=31536000" always;
}
```

### 10.2 数据加密

```java
// 敏感数据加密工具类
@Component
public class EncryptionUtil {
    @Value("${encryption.key}")
    private String encryptionKey;

    // AES-256加密
    public String encrypt(String plainText) {
        // 使用AES-256-GCM模式
        SecretKeySpec keySpec = new SecretKeySpec(
            encryptionKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        // ...
        return Base64.encode(encrypted);
    }

    // 解密
    public String decrypt(String encryptedText) {
        // ...
    }
}

// 需要加密的字段：
// · 身份证号
// · 银行卡号
// · 真实姓名
// · 详细地址
```

### 10.3 输入验证与防护

```java
// 防XSS过滤器
@Component
public class XssFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) {
        chain.doFilter(new XssRequestWrapper(
            (HttpServletRequest) request), response);
    }
}

// 防SQL注入：使用JPA参数化查询（默认安全）
@Query("SELECT t FROM Transaction t WHERE t.userId = :userId")
List<Transaction> findByUserId(@Param("userId") Long userId);

// 防CSRF：Spring Security配置
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        return http.build();
    }
}
```

### 10.4 认证安全

```
JWT安全配置：
├─ Access Token有效期：2小时
├─ Refresh Token有效期：7天
├─ Token存储：HttpOnly Cookie（防XSS）
├─ 单设备登录（可选）
└─ 登录失败锁定：5次失败锁定30分钟

密码安全：
├─ BCrypt加密（cost=12）
├─ 密码强度要求：8位以上，包含大小写+数字
└─ 防止密码暴力破解（限流）
```

### 10.5 敏感数据脱敏

```java
// 日志脱敏
@Slf4j
public class LogUtil {
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) return idCard;
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }
}

// 响应脱敏注解
@JsonSerialize(using = PhoneMaskSerializer.class)
private String phone;
```

---

## 11. 测试策略

### 11.1 单元测试

```java
// 使用JUnit 5 + Mockito
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void testGetMonthlyStatistics() {
        // Given
        Long userId = 1L;
        YearMonth yearMonth = YearMonth.of(2026, 1);
        List<Transaction> mockTransactions = Arrays.asList(
            createTransaction(1000, TransactionType.INCOME),
            createTransaction(500, TransactionType.EXPENSE)
        );
        when(transactionRepository.findByUserIdAndTransactionDateBetween(
            eq(userId), any(), any()))
            .thenReturn(mockTransactions);

        // When
        MonthlyStatistics result = transactionService
            .getMonthlyStatistics(userId, yearMonth);

        // Then
        assertEquals(new BigDecimal("1000"), result.getTotalIncome());
        assertEquals(new BigDecimal("500"), result.getTotalExpense());
        assertEquals(new BigDecimal("500"), result.getBalance());
    }
}

// 覆盖率目标：>80%
// 使用JaCoCo生成覆盖率报告
```

### 11.2 集成测试

```java
// 使用Testcontainers进行数据库集成测试
@SpringBootTest
@Testcontainers
class TransactionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:14-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateTransaction() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(1L);

        ResponseEntity<ApiResponse> response = restTemplate
            .postForEntity("/api/transactions", request, ApiResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

### 11.3 API测试

```java
// 使用MockMvc测试Controller
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void testCreateTransaction() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":100,\"type\":2,\"categoryId\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));
    }
}
```

### 11.4 E2E测试（前端）

```typescript
// 使用Playwright进行E2E测试
import { test, expect } from '@playwright/test';

test.describe('记账功能', () => {
  test('手动记账流程', async ({ page }) => {
    // 登录
    await page.goto('/login');
    await page.fill('[name="username"]', 'testuser');
    await page.fill('[name="password"]', 'password123');
    await page.click('button[type="submit"]');

    // 导航到记账页
    await page.click('text=记账');

    // 填写记账信息
    await page.fill('[name="amount"]', '50');
    await page.selectOption('[name="category"]', '餐饮');
    await page.fill('[name="description"]', '午餐');

    // 提交
    await page.click('button:has-text("保存")');

    // 验证
    await expect(page.locator('.success-message')).toBeVisible();
  });
});
```

---

## 12. CI/CD流程

### 12.1 GitHub Actions工作流

```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  # 后端构建
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'corretto'

      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Code Style Check
        run: mvn checkstyle:check

      - name: Run Tests
        run: mvn test

      - name: Build
        run: mvn package -DskipTests

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: target/site/jacoco/jacoco.xml

  # 前端构建
  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '18'

      - name: Install pnpm
        run: npm install -g pnpm

      - name: Install dependencies
        run: pnpm install
        working-directory: ./frontend

      - name: Lint
        run: pnpm lint
        working-directory: ./frontend

      - name: Build
        run: pnpm build
        working-directory: ./frontend

  # Docker构建与推送
  docker:
    needs: [backend, frontend]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4

      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build and push
        uses: docker/build-push-action@v5
        with:
          push: true
          tags: your-repo/finance-planner:latest

  # 部署到测试环境
  deploy-staging:
    needs: docker
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Deploy to staging
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.STAGING_HOST }}
          username: ${{ secrets.STAGING_USER }}
          key: ${{ secrets.STAGING_SSH_KEY }}
          script: |
            cd /app
            docker-compose pull
            docker-compose up -d
```

### 12.2 部署流程图

```
代码提交
    ↓
代码检查（ESLint/Checkstyle）
    ↓
单元测试
    ↓
构建制品
    ↓
构建Docker镜像
    ↓
推送到镜像仓库
    ↓
部署到测试环境
    ↓
E2E测试
    ↓
人工审核
    ↓
部署到生产环境
    ↓
健康检查
    ↓
回滚机制（如失败）
```

---

## 13. 日志与监控

### 13.1 日志规范

```yaml
# logback-spring.xml配置
logging:
  level:
    root: INFO
    com.finance.planner: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/finance-planner.log
    max-size: 100MB
    max-history: 30
```

```java
// 日志规范
@Slf4j
public class TransactionService {

    public void createTransaction(TransactionRequest request, Long userId) {
        // 入口日志
        log.info("Creating transaction, userId={}, amount={}",
            userId, request.getAmount());

        try {
            // 业务逻辑...

            // 成功日志
            log.info("Transaction created successfully, id={}",
                transaction.getId());
        } catch (Exception e) {
            // 异常日志
            log.error("Failed to create transaction, userId={}, error={}",
                userId, e.getMessage(), e);
            throw e;
        }
    }
}

// 日志格式：JSON结构化（生产环境）
// {"timestamp":"2026-01-27T10:00:00","level":"INFO","message":"...","userId":1}
```

### 13.2 监控体系

```
监控架构：
┌─────────────────────────────────────────────────────┐
│  应用层                                              │
│  ┌─────────────────┐    ┌─────────────────┐        │
│  │ Spring Boot      │───>│ Actuator        │        │
│  │ Application      │    │ Endpoints       │        │
│  └─────────────────┘    └────────┬────────┘        │
│                                  │                   │
└──────────────────────────────────┼───────────────────┘
                                   ↓
┌─────────────────────────────────────────────────────┐
│  采集层                                              │
│  ┌─────────────────┐    ┌─────────────────┐        │
│  │ Prometheus      │    │ Filebeat        │        │
│  │ (指标采集)       │    │ (日志采集)       │        │
│  └────────┬────────┘    └────────┬────────┘        │
│           │                      │                   │
└───────────┼──────────────────────┼───────────────────┘
            ↓                      ↓
┌─────────────────────────────────────────────────────┐
│  存储层                                              │
│  ┌─────────────────┐    ┌─────────────────┐        │
│  │ Prometheus      │    │ Elasticsearch   │        │
│  │ TSDB            │    │                 │        │
│  └────────┬────────┘    └────────┬────────┘        │
│           │                      │                   │
└───────────┼──────────────────────┼───────────────────┘
            ↓                      ↓
┌─────────────────────────────────────────────────────┐
│  展示层                                              │
│  ┌─────────────────┐    ┌─────────────────┐        │
│  │ Grafana         │    │ Kibana          │        │
│  │ (指标可视化)     │    │ (日志可视化)     │        │
│  └─────────────────┘    └─────────────────┘        │
└─────────────────────────────────────────────────────┘
```

### 13.3 告警规则

```yaml
# Prometheus告警规则
groups:
  - name: finance-planner-alerts
    rules:
      # API响应时间过长
      - alert: HighResponseTime
        expr: http_server_requests_seconds_max > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API响应时间过长"

      # 错误率过高
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.05
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "错误率超过5%"

      # 内存使用过高
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.85
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "JVM内存使用超过85%"
```

### 13.4 关键指标Dashboard

```
核心监控指标：
├─ 系统指标
│   ├─ CPU使用率
│   ├─ 内存使用率
│   ├─ 磁盘IO
│   └─ 网络流量
│
├─ 应用指标
│   ├─ QPS（每秒请求数）
│   ├─ 响应时间（P50/P95/P99）
│   ├─ 错误率
│   └─ 活跃连接数
│
├─ 业务指标
│   ├─ 在线用户数
│   ├─ API调用量（按接口）
│   ├─ AI对话请求量
│   └─ OCR调用量
│
└─ 数据库指标
    ├─ 连接池使用率
    ├─ 慢查询数量
    └─ 缓存命中率
```

---

## 14. 高可用与灾备

### 14.1 高可用架构

```
┌─────────────────────────────────────────────────────────────┐
│                         用户                                 │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                      CDN + WAF                               │
│              (静态资源加速 + Web应用防火墙)                    │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                   负载均衡（SLB）                             │
│                    健康检查 + 自动剔除                        │
└────────────────────────────┬────────────────────────────────┘
                             │
              ┌──────────────┼──────────────┐
              ↓              ↓              ↓
        ┌─────────┐    ┌─────────┐    ┌─────────┐
        │ App 1   │    │ App 2   │    │ App N   │
        │ (可用区A)│    │ (可用区B)│    │ (可用区C)│
        └────┬────┘    └────┬────┘    └────┬────┘
             │              │              │
             └──────────────┼──────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   数据层高可用                                │
│  ┌──────────────────┐    ┌──────────────────┐              │
│  │  PostgreSQL      │    │  Redis           │              │
│  │  主从复制        │    │  Sentinel集群     │              │
│  │  ┌─────┐ ┌─────┐│    │  ┌─────┐ ┌─────┐ │              │
│  │  │主库 │→│从库 ││    │  │主节点│→│从节点│ │              │
│  │  └─────┘ └─────┘│    │  └─────┘ └─────┘ │              │
│  └──────────────────┘    └──────────────────┘              │
└─────────────────────────────────────────────────────────────┘
```

### 14.2 容灾设计

```
容灾级别：
├─ 应用层容灾
│   ├─ 多实例部署（>=2个实例）
│   ├─ 跨可用区部署
│   ├─ 健康检查（每30秒）
│   └─ 自动重启（容器编排）
│
├─ 数据层容灾
│   ├─ PostgreSQL主从同步
│   ├─ Redis Sentinel自动故障转移
│   └─ 数据异地备份（另一地域）
│
└─ 服务降级
    ├─ AI服务降级：返回预设回复
    ├─ OCR服务降级：提示用户手动输入
    └─ 非核心功能降级：隐藏功能入口

容灾指标：
├─ RTO（恢复时间目标）：< 4小时
├─ RPO（恢复点目标）：< 1小时
└─ 可用性目标：99.9%（全年停机<8.76小时）
```

### 14.3 故障演练

```
演练计划（每季度一次）：
├─ 演练1：单节点故障
│   └─ 手动停止一个应用实例，验证负载均衡切换
│
├─ 演练2：数据库主从切换
│   └─ 模拟主库故障，验证从库自动提升
│
├─ 演练3：全链路压测
│   └─ 模拟10倍流量，验证系统扩展能力
│
└─ 演练4：数据恢复
    └─ 从备份恢复数据库，验证恢复流程
```

---

## 15. 数据备份策略

### 15.1 备份策略

```
备份类型：
┌──────────────────────────────────────────────────────┐
│  数据库备份                                           │
│  ├─ 全量备份：每日凌晨3:00（业务低峰）               │
│  ├─ 增量备份：WAL日志实时归档                        │
│  └─ 保留周期：30天                                   │
├──────────────────────────────────────────────────────┤
│  文件备份                                             │
│  ├─ OSS多副本存储（3副本）                           │
│  ├─ 跨地域备份（杭州→上海）                          │
│  └─ 保留周期：永久（用户文件）                       │
├──────────────────────────────────────────────────────┤
│  配置备份                                             │
│  ├─ Git版本控制                                      │
│  ├─ 配置中心快照                                     │
│  └─ 保留周期：永久                                   │
└──────────────────────────────────────────────────────┘
```

### 15.2 备份脚本

```bash
#!/bin/bash
# 数据库备份脚本

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/backup/postgres
DB_NAME=finance_planner

# 创建备份目录
mkdir -p $BACKUP_DIR

# 执行备份
pg_dump -h localhost -U postgres -Fc $DB_NAME > $BACKUP_DIR/backup_$DATE.dump

# 上传到OSS
ossutil cp $BACKUP_DIR/backup_$DATE.dump oss://finance-backup/postgres/

# 清理30天前的本地备份
find $BACKUP_DIR -name "*.dump" -mtime +30 -delete

# 记录日志
echo "[$DATE] Backup completed" >> /var/log/backup.log
```

### 15.3 恢复流程

```
恢复步骤：
1. 评估故障范围
   └─ 确认需要恢复的数据范围和时间点

2. 选择备份版本
   └─ 从OSS下载最近的全量备份

3. 准备恢复环境
   └─ 新建临时数据库或使用灾备实例

4. 执行恢复
   └─ pg_restore -d finance_planner backup.dump

5. 应用增量日志（如需要）
   └─ 恢复WAL日志到指定时间点

6. 验证数据
   └─ 检查关键表数据完整性

7. 切换流量
   └─ 将应用指向恢复后的数据库

8. 事后复盘
   └─ 分析故障原因，完善预防措施
```

### 15.4 恢复演练

```
演练计划：每月一次
演练内容：
├─ 从最近备份恢复到测试环境
├─ 验证数据完整性
├─ 记录恢复耗时
└─ 更新恢复文档

目标指标：
├─ 恢复时间：< 2小时
└─ 数据完整性：100%
```

---

## 16. API限流策略

### 16.1 限流规则

```java
// 使用Bucket4j实现限流
@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket globalBucket() {
        // 全局限流：1000 QPS
        return Bucket.builder()
            .addLimit(Bandwidth.simple(1000, Duration.ofSeconds(1)))
            .build();
    }
}

// 用户级限流
@Service
public class RateLimitService {

    private final Map<Long, Bucket> userBuckets = new ConcurrentHashMap<>();

    public Bucket getUserBucket(Long userId) {
        return userBuckets.computeIfAbsent(userId, id ->
            Bucket.builder()
                // 每分钟100次请求
                .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))
                .build()
        );
    }
}
```

### 16.2 限流配置

```yaml
限流规则：
├─ 全局限流
│   └─ 1000 QPS（超过返回429）
│
├─ 用户级限流
│   ├─ 普通接口：100次/分钟
│   └─ 敏感接口（登录）：5次/分钟
│
├─ AI对话限流
│   ├─ 免费用户：10次/天
│   ├─ 付费用户：无限制
│   └─ 单次对话Token限制：4096
│
└─ OCR识别限流
    ├─ 免费用户：10次/天
    └─ 付费用户：100次/天
```

### 16.3 限流响应

```json
// 超过限流返回429
{
    "code": 429,
    "message": "请求过于频繁，请稍后再试",
    "data": {
        "retryAfter": 60
    }
}
```

---

## 附录

### A. 常见问题FAQ

**Q: 为什么选择PostgreSQL而不是MySQL？**
A: PostgreSQL对JSON字段支持更好（存储AI对话记录），全文搜索功能更强。

**Q: 为什么不用微服务架构？**
A: MVP阶段单体应用开发效率更高，用户量小时性能足够，后期可拆分。

**Q: DeepSeek API调用失败怎么办？**
A: 实现重试机制（最多3次），失败后返回友好提示。

---

### B. 技术决策记录

**ADR-001: 选择Spring Boot 3.x**
- 理由：支持Java 17新特性，性能更好，生态成熟
- 风险：学习曲线略高
- 决策日期：2026-01-26

**ADR-002: 选择Vue 3 Composition API**
- 理由：代码组织更清晰，TypeScript支持更好
- 风险：需要适应新写法
- 决策日期：2026-01-26

---

### C. 第三方依赖清单

**后端核心依赖**：
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.12.5</version>
    </dependency>
</dependencies>
```

**前端核心依赖**：
```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.0.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.5.0",
    "axios": "^1.6.0",
    "echarts": "^5.4.0"
  }
}
```

---

**文档完成！✅**


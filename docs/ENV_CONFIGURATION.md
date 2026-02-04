# Environment Configuration Guide

本文档说明如何配置 Come Rich 项目的环境变量。

## 快速开始

### 1. 复制示例文件

```bash
# 根目录（后端使用）
cp .env.example .env

# 前端目录（可选，用于覆盖默认值）
cp finance-planner-frontend/.env.example finance-planner-frontend/.env.local

# Docker 环境（可选）
cp .env.docker.example .env.docker
```

### 2. 编辑 `.env` 文件，填入实际值

**必须配置的项目：**
- `DB_PASSWORD` - 数据库密码
- `JWT_SECRET` - JWT 密钥（至少 64 字符）
- `DEEPSEEK_API_KEY` - DeepSeek API 密钥

## 文件说明

| 文件 | 用途 | 是否提交到 Git |
|------|------|---------------|
| `.env.example` | 后端环境变量模板 | ✅ 是 |
| `.env` | 实际后端配置（含敏感信息） | ❌ 否 |
| `.env.docker.example` | Docker 环境变量模板 | ✅ 是 |
| `.env.docker` | 实际 Docker 配置 | ❌ 否 |
| `frontend/.env.example` | 前端环境变量模板 | ✅ 是 |
| `frontend/.env.development` | 开发环境配置 | ✅ 是 |
| `frontend/.env.production` | 生产环境配置 | ✅ 是 |
| `frontend/.env.local` | 本地覆盖配置 | ❌ 否 |

## 后端配置详解

### 数据库配置

```properties
DB_HOST=localhost          # 数据库主机
DB_PORT=5432               # 数据库端口
DB_NAME=finance_planner    # 数据库名称
DB_USERNAME=finance        # 数据库用户名
DB_PASSWORD=xxx            # 数据库密码（必填）
```

### Redis 配置

```properties
REDIS_HOST=localhost       # Redis 主机
REDIS_PORT=6379            # Redis 端口
REDIS_DATABASE=0           # Redis 数据库索引
REDIS_PASSWORD=            # Redis 密码（可选）
```

### JWT 配置

```properties
JWT_SECRET=xxx             # JWT 签名密钥（至少 64 字符）
JWT_EXPIRATION=86400000    # Token 过期时间（毫秒）
```

**生成安全的 JWT Secret：**

```bash
# Linux/macOS
openssl rand -base64 64

# PowerShell
[System.Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
```

### AI 提供商配置

```properties
AI_ACTIVE_PROVIDER=deepseek    # 活跃提供商: deepseek | moonshot | qwen

# DeepSeek (https://platform.deepseek.com/)
DEEPSEEK_API_KEY=xxx

# Moonshot (https://platform.moonshot.cn/)
MOONSHOT_API_KEY=xxx

# Qwen (https://dashscope.aliyun.com/)
QWEN_API_KEY=xxx

# 通用 AI 设置
AI_DAILY_LIMIT=10              # 每日免费对话次数
```

### OCR 配置

```properties
# 百度 OCR (https://cloud.baidu.com/product/ocr)
BAIDU_OCR_API_KEY=xxx
BAIDU_OCR_SECRET_KEY=xxx
```

## 前端配置详解

前端使用 Vite 的环境变量机制，所有变量必须以 `VITE_` 前缀开头。

### API 配置

```properties
VITE_API_BASE_URL=http://localhost:8080    # 后端 API 地址
VITE_API_TIMEOUT=30000                      # 请求超时（毫秒）
```

### 功能开关

```properties
VITE_FEATURE_OCR_ENABLED=true              # OCR 功能
VITE_FEATURE_AI_CHAT_ENABLED=true          # AI 对话功能
VITE_FEATURE_INVESTMENT_ENABLED=true       # 投资建议功能
```

### 在代码中使用

```typescript
import { API_BASE_URL, FEATURE_FLAGS, isFeatureEnabled } from '@/utils/env'

// 使用 API 地址
console.log(API_BASE_URL)

// 检查功能是否启用
if (isFeatureEnabled('ocrEnabled')) {
  // OCR 功能已启用
}
```

## Docker 环境配置

使用 Docker Compose 时，可以通过 `.env.docker` 文件配置环境变量：

```bash
# 复制模板
cp .env.docker.example .env.docker

# 使用自定义配置启动
docker-compose -f docker-compose-dev.yml --env-file .env.docker up -d
```

## 环境变量优先级

### 后端 (Spring Boot)

1. 系统环境变量
2. `.env` 文件（通过 IDE 或启动脚本加载）
3. `application.yml` 中的默认值

### 前端 (Vite)

1. `.env.local`（最高优先级，不提交）
2. `.env.[mode].local`
3. `.env.[mode]`（如 `.env.development`）
4. `.env`

## 安全注意事项

1. **永远不要** 将包含真实密钥的 `.env` 文件提交到 Git
2. 使用强密码和长密钥（JWT Secret 至少 64 字符）
3. 定期轮换 API 密钥
4. 生产环境使用专用的密钥管理服务（如 AWS Secrets Manager、HashiCorp Vault）
5. 限制 API 密钥的权限范围

## 故障排除

### 环境变量未生效

1. 确认文件名正确（注意 `.env.local` 的点号）
2. 重启开发服务器
3. 检查变量名是否正确（前端必须有 `VITE_` 前缀）

### IDE 中加载 .env

**IntelliJ IDEA:**
- 安装 "EnvFile" 插件
- 在运行配置中添加 `.env` 文件

**VS Code:**
- 使用 "DotENV" 扩展获得语法高亮
- 使用 launch.json 的 `envFile` 配置

### Spring Boot 加载 .env

添加 `spring-dotenv` 依赖或使用 IDE 插件。开发时也可以手动 export：

```bash
# Linux/macOS
export $(cat .env | xargs)
mvn spring-boot:run

# PowerShell
Get-Content .env | ForEach-Object { if ($_ -match '^([^#][^=]*)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2]) } }
mvn spring-boot:run
```

# Docker 部署指南

本文档说明如何使用 Docker 一键部署「IDC 基础设施管理系统」全套服务（单体后端 + PostgreSQL + 前端，含配件中心与内置 RBAC）。

## 架构概览

```
浏览器 (:3000)
    │
    ▼
┌─────────────┐     ┌────────────────────────────┐     ┌──────────────┐
│  Frontend   │────▶│          Backend           │────▶│ PostgreSQL   │
│  (Nginx)    │     │  idc-app（Spring Boot）     │     │ idc_manager  │
│  :3000→80   │     │           :8080            │     │    :5433     │
└─────────────┘     └────────────────────────────┘     └──────────────┘
```

仅 3 个容器：`dc-postgres`、`dc-backend`、`dc-frontend`。后端为单体应用，认证 / 用户中心 / 全部业务接口均由 `idc-app` 提供。

## 环境要求

| 组件 | 最低版本 |
|------|----------|
| Docker | 24.0+ |
| Docker Compose | v2.20+ |
| 可用内存 | 2 GB+ |
| 磁盘空间 | 3 GB+ |

## 快速部署

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd IDC-Manager
```

### 2. 一键启动全部服务

```bash
docker compose up -d --build
```

首次构建会下载 Maven / Node 依赖并编译后端与前端，耗时约 3~8 分钟（视网络而定）。

### 3. 查看启动状态

```bash
docker compose ps
docker compose logs -f
```

等待所有容器状态为 `running`，且 `dc-postgres` 健康检查通过。

### 4. 访问系统

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端（登录页） |
| http://localhost:8080/api/servers | 服务器 API（需 Bearer Token） |
| http://localhost:8080/api/accessory-center/requests | 配件中心 API |
| http://localhost:8080/swagger-ui.html | API 文档（Swagger） |

### 演示账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `123456` | 全局管理员 |
| `accessory_admin` | `123456` | 配件管理员 |
| `user` | `123456` | 普通用户 |

首次启动后，数据库由 JPA 自动建表：`RbacDataInitializer` 幂等写入三个演示账号、三个角色与全部权限点菜单；业务 `DataInitializer` 写入示例服务器/配件/备件数据。

## 服务说明

| 容器名 | 镜像/构建 | 端口 | 说明 |
|--------|-----------|------|------|
| dc-postgres | postgres:16-alpine | 5433 | 单库 `idc_manager`（业务 + RBAC） |
| dc-backend | 本地构建（`docker/Dockerfile.backend` 多阶段） | 8080 | 单体后端：认证 / 用户中心 / 业务 |
| dc-frontend | 本地构建（`frontend/Dockerfile`） | 3000→80 | React 18 + Ant Design 5 前端（Nginx） |

## 数据库配置

单库 `idc_manager`，容器内连接 `jdbc:postgresql://postgres:5432/idc_manager`，默认凭据 `dcasset` / `dcasset123`。

后端可通过环境变量覆盖：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_HOST` | 数据库主机 | `postgres`（compose）/ `localhost` |
| `DB_PORT` | 端口 | `5432` |
| `DB_NAME` | 库名 | `idc_manager` |
| `DB_USER` / `DB_PASSWORD` | 凭据 | `dcasset` / `dcasset123` |
| `JWT_SECRET` | JWT 签名密钥 | 内置默认值（生产必改） |
| `JWT_ACCESS_VALIDITY` | access token 有效期（秒） | `7200` |
| `JWT_REFRESH_VALIDITY` | refresh token 有效期（秒） | `604800` |

> **生产环境请务必修改数据库密码与 `JWT_SECRET`**。

## 常用运维命令

### 停止服务

```bash
docker compose down
```

### 停止并清除数据卷（重置数据库）

```bash
docker compose down -v
```

### 仅重建某个服务

```bash
docker compose up -d --build backend
docker compose up -d --build frontend
```

### 查看单个服务日志

```bash
docker compose logs -f backend
docker compose logs -f dc-postgres
```

### 进入数据库

```bash
docker exec -it dc-postgres psql -U dcasset -d idc_manager
```

## 文件结构

```
.
├── docker-compose.yml
├── docker/
│   └── Dockerfile.backend        # 多阶段构建单应用（构建上下文为仓库父目录）
├── idc-app/                      # 单体后端（JDK 17 + Spring Boot）
├── frontend/
│   ├── Dockerfile
│   └── nginx.conf
└── scripts/start-local.sh
```

## 故障排查

| 现象 | 可能原因 | 处理方式 |
|------|----------|----------|
| 登录 401 | 数据库未就绪 / 账号待审批 | `docker compose logs backend`；确认 `dc-postgres` 健康 |
| 业务 API 401 | 令牌过期或 `JWT_SECRET` 变更 | 重新登录；确认密钥配置未变 |
| 403 | 角色无权限点 | 用 `admin` / `accessory_admin` 测审批出库 |
| 后端启动失败 | 数据库连不上 | 确认 `DB_HOST=postgres` 与依赖顺序（compose 已配 `depends_on: healthy`） |
| 端口冲突 | 本机占用 | 调整 compose 端口映射 |

### 健康检查顺序

1. PostgreSQL（`pg_isready` 健康检查）
2. backend（依赖数据库健康后启动，首次自动建库表与种子数据）
3. frontend

## 生产环境建议

- 修改数据库默认密码与 `JWT_SECRET`
- 为 PostgreSQL 配置持久化备份
- 关闭或限制 Swagger 对外暴露
- 使用 HTTPS 反向代理前端

# Docker 部署指南

本文档说明如何使用 Docker 一键部署「IDC 基础设施管理系统」全套服务（含配件中心与生产级 RBAC）。

## 架构概览

```
浏览器 (:3000)
    │
    ▼
┌─────────────┐     ┌──────────────┐     ┌──────────────────────┐
│  Frontend   │────▶│ pig-gateway  │────▶│  pig-dc-server-biz   │
│  (Nginx)    │     │    :9999     │     │       :8081          │
└─────────────┘     └──────┬───────┘     └──────────┬───────────┘
                           │                        │
              ┌────────────┼────────────┐  ┌──────────────────────────┐
              ▼            ▼            ▼  │  pig-dc-accessory-biz    │
        ┌──────────┐ ┌──────────┐ ┌──────────┐│        :8082             │
        │ pig-auth │ │pig-upms  │ │ Redis    │└──────────────────────────┘
        │  :3001   │ │  :4000   │ │  :6379   │
        └────┬─────┘ └────┬─────┘ └──────────┘
             │            │
             ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │  MySQL   │ │PostgreSQL│ │  Nacos   │
        │  :3306   │ │  :5432   │ │  :8848   │
        └──────────┘ └──────────┘ └──────────┘
```

> `pig-auth` 使用 **3001**，避免与前端宿主端口冲突。  
> Docker 构建上下文需包含 `../pig` 与 `IDC-Manager` 两个同级目录。

## 环境要求

| 组件 | 最低版本 |
|------|----------|
| Docker | 24.0+ |
| Docker Compose | v2.20+ |
| 可用内存 | 4 GB+（推荐 8 GB） |
| 磁盘空间 | 5 GB+ |

## 快速部署

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd IDC-Manager
# 同级目录需有 PigX 4.1.0 源码 ../pig
```

### 2. 一键启动全部服务

```bash
docker compose up -d --build
```

首次构建会下载 Maven / Node 依赖并编译后端与前端，耗时约 5~15 分钟（视网络而定）。

### 3. 查看启动状态

```bash
docker compose ps
docker compose logs -f
```

等待所有容器状态为 `running`，且 `dc-postgres`、`dc-mysql`、`dc-nacos` 健康检查通过。

### 4. 访问系统

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端（登录页） |
| http://localhost:9999/api/servers | 服务器 API（需 Bearer Token） |
| http://localhost:9999/api/accessory-center/requests | 配件中心 API |
| http://localhost:8081/swagger-ui.html | 服务器服务 Swagger |
| http://localhost:8082/swagger-ui.html | 配件服务 Swagger |
| http://localhost:8848/nacos | Nacos 控制台 |

### 演示账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `123456` | 全局管理员 |
| `accessory_admin` | `123456` | 配件管理员 |
| `user` | `123456` | 普通用户 |

首次启动后，业务库由 JPA 建表并写入示例数据；MySQL 依次导入 `pig.sql`、`pig_config.sql`、IDC 网关路由与配件中心 RBAC 种子。

## 服务说明

| 容器名 | 镜像/构建 | 端口 | 说明 |
|--------|-----------|------|------|
| dc-postgres | postgres:18-alpine | 5432 | 业务库 `dc_server_db` / `dc_accessory_db` |
| dc-mysql | mysql:8.0 | 3306 | PigX `pig` 库（用户/角色/菜单/OAuth） |
| dc-redis | redis:7-alpine | 6379 | OAuth opaque token / 网关 |
| dc-nacos | nacos/nacos-server:v2.4.3 | 8848, 9848 | 服务注册与发现 |
| pig-auth | 本地构建 | 见 compose | OAuth2 授权服务器 |
| pig-upms-biz | 本地构建 | 4000 | 用户权限管理 |
| pig-dc-server-biz | 本地构建 | 8081 | 服务器管理 |
| pig-dc-accessory-biz | 本地构建 | 8082 | 配件 / 备件 / 配件中心 |
| pig-gateway | 本地构建 | 9999 | API 网关 |
| dc-frontend | 本地构建 | 3000→80 | Vue3 前端（Nginx） |

## 数据库配置

### PostgreSQL（业务）

| 微服务 | 数据库名 | 连接地址（容器内） |
|--------|----------|-------------------|
| pig-dc-server-biz | `dc_server_db` | `jdbc:postgresql://postgres:5432/dc_server_db` |
| pig-dc-accessory-biz | `dc_accessory_db` | `jdbc:postgresql://postgres:5432/dc_accessory_db` |

默认凭据：`dcasset` / `dcasset123`。初始化脚本：`docker/init-db.sql`。

### MySQL（身份权限）

| 用途 | 库名 | 说明 |
|------|------|------|
| PigX UPMS / Auth | `pig` | `db-pig/pig.sql` + 配件中心 RBAC 种子 |
| Nacos 配置中心 | `pig_config` | `db-pig/pig_config.sql` + IDC 网关路由补丁 |

默认 root 密码见 `docker-compose.yml`（开发默认 `root`）。

> **生产环境请务必修改默认密码**。

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
docker compose up -d --build pig-dc-accessory-biz
docker compose up -d --build frontend
```

### 查看单个服务日志

```bash
docker compose logs -f pig-dc-server-biz
docker compose logs -f pig-dc-accessory-biz
docker compose logs -f pig-auth
docker compose logs -f pig-gateway
docker compose logs -f dc-mysql
```

### 进入数据库

```bash
docker exec -it dc-postgres psql -U dcasset -d dc_server_db
docker exec -it dc-mysql mysql -uroot -proot pig
```

## 自定义配置

```env
# .env 示例
POSTGRES_PASSWORD=your_secure_password
MYSQL_ROOT_PASSWORD=your_secure_password
SPRING_DATASOURCE_PASSWORD=your_secure_password
```

| 变量名 | 说明 |
|--------|------|
| `SPRING_DATASOURCE_URL` | 业务库 JDBC |
| `MYSQL_HOST` / `MYSQL_*` | Auth / UPMS 数据源 |
| `NACOS_SERVER_ADDR` | Nacos 地址 |
| Redis 相关 | Auth 与资源服务须共享同一 Redis |

## 文件结构

```
.
├── docker-compose.yml
├── docker/
│   ├── Dockerfile.pig-backend
│   ├── Dockerfile.pig-platform
│   ├── init-db.sql
│   └── mysql-init/
│       ├── 02-gateway-idc-routes.sql
│       └── 02-accessory-center-rbac.sql
├── db-pig/                     # pig.sql / pig_config.sql（符号链接或拷贝）
├── config/                     # *-local.yml（本地非 Docker）
├── frontend/
│   ├── Dockerfile
│   └── nginx.conf
└── scripts/start-local.sh
```

## 故障排查

| 现象 | 可能原因 | 处理方式 |
|------|----------|----------|
| 登录 401 | auth / MySQL / Redis 未就绪 | `docker compose logs pig-auth`；确认 MySQL 已导入种子 |
| 业务 API 401 | 资源服务无法 introspect Redis token | 确认与 auth 共用 Redis |
| 403 | 角色无权限点 | 用 `admin` / `accessory_admin` 测审批出库 |
| 网关 503 | 服务未注册 Nacos | 等待 30~60 秒；查对应服务日志 |
| 端口冲突 | 本机占用 | 调整 compose 端口映射 |

### 健康检查顺序

1. PostgreSQL、MySQL、Redis  
2. Nacos  
3. pig-upms-biz → pig-auth  
4. pig-dc-* → pig-gateway  
5. frontend  

## 生产环境建议

- 修改所有默认密码与 OAuth client secret  
- 为 MySQL / PostgreSQL 配置持久化备份  
- 关闭或限制 Swagger 对外暴露  
- 使用 HTTPS 与安全的 Redis 访问策略  

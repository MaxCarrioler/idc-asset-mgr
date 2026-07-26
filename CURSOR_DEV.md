# Cursor 容器远程开发指南

本文档说明如何在 Linux 环境的 Cursor 编辑器中，通过 Dev Containers（开发容器）连接 Docker 容器进行远程开发。

## 方案概览

开发模式采用**混合架构**：

- **基础设施容器化**：PostgreSQL + Nacos 在 Docker 中运行
- **应用本地运行**：后端微服务、网关、前端在开发容器内通过 Maven / npm 热启动
- **源码实时同步**：项目目录挂载到开发容器，修改即时生效

```
┌─────────────────────────────────────────────────────┐
│  Cursor 编辑器（Linux 宿主机）                        │
│       │                                             │
│       ▼  Dev Containers 连接                        │
│  ┌─────────────────────────────────────────────┐    │
│  │  devcontainer（Java 17 + Node 20）          │    │
│  │  ├── mvn spring-boot:run  → 微服务 :8081/82 │    │
│  │  ├── mvn spring-boot:run  → 网关    :8080  │    │
│  │  └── npm run dev          → 前端    :3000  │    │
│  └──────────────┬──────────────────────────────┘    │
│                 │ Docker 网络 (dc-network)           │
│  ┌──────────────┴──────────────────────────────┐    │
│  │  postgres :5432    nacos :8848              │    │
│  └─────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

## 前置条件

### 宿主机环境

| 组件 | 要求 |
|------|------|
| 操作系统 | Linux（Ubuntu 22.04+ / Debian 12+ 推荐） |
| Docker | 24.0+，当前用户已加入 `docker` 组 |
| Docker Compose | v2.20+ |
| Cursor | 最新版，已安装 **Dev Containers** 扩展 |

### 验证 Docker 可用

```bash
docker --version
docker compose version
docker ps
```

若 `docker ps` 需要 `sudo`，请将用户加入 docker 组后重新登录：

```bash
sudo usermod -aG docker $USER
```

## 配置文件说明

项目已预置以下开发容器配置：

```
.
├── .devcontainer/
│   └── devcontainer.json       # Cursor / VS Code Dev Containers 配置
├── docker-compose.yml            # 基础编排（含全部生产服务定义）
├── docker-compose.dev.yml        # 开发模式覆盖（仅启动基础设施 + 开发容器）
└── scripts/
    └── dev-env.sh                # 开发环境变量快捷脚本
```

### devcontainer.json 关键配置

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `dockerComposeFile` | `docker-compose.yml` + `docker-compose.dev.yml` | 合并编排，开发模式不启动应用容器 |
| `service` | `devcontainer` | Cursor 连接的目标容器 |
| `workspaceFolder` | `/workspace` | 容器内工作目录 |
| Java | 17（基础镜像内置） | 后端编译运行 |
| Node.js | 20（devcontainer feature） | 前端开发 |
| `postCreateCommand` | `mvn dependency:resolve` + `npm install` | 首次进入容器自动安装依赖 |

### 开发模式与生产模式的区别

| 模式 | 启动命令 | 运行的容器 |
|------|----------|-----------|
| 生产部署 | `docker compose up -d --build` | 全部 6 个服务 |
| 远程开发 | Cursor Reopen in Container | postgres + nacos + devcontainer |

`docker-compose.dev.yml` 通过 `profiles: production` 标记应用服务，开发模式下不会启动它们。

## 操作步骤

### 第一步：打开项目

在 Linux 宿主机上用 Cursor 打开项目根目录：

```bash
cd /path/to/数据中心配件流通系统
cursor .
```

### 第二步：进入开发容器

1. 按 `Ctrl+Shift+P` 打开命令面板
2. 输入并选择：**Dev Containers: Reopen in Container**
3. 等待 Docker 构建并启动容器（首次约 3~10 分钟）

Cursor 会自动：

- 启动 `postgres` 和 `nacos` 容器
- 创建 `devcontainer` 并挂载项目源码
- 执行 `postCreateCommand` 安装 Maven 和 npm 依赖
- 在容器内重新打开工作区

### 第三步：确认基础设施就绪

在 Cursor 集成终端（已在容器内）执行：

```bash
# 检查容器状态
docker compose ps

# 验证 PostgreSQL
docker exec dc-postgres pg_isready -U dcasset -d dc_server_db

# 验证 Nacos（等待约 30 秒）
curl -s -o /dev/null -w "%{http_code}" http://nacos:8848/nacos/
# 返回 200 表示就绪
```

### 第四步：编译后端

```bash
mvn clean install -DskipTests
```

### 第五步：加载环境变量

```bash
source scripts/dev-env.sh
```

### 第六步：启动微服务（需要 3 个终端）

**终端 1 — 服务器服务**

```bash
cd dc-server-service
SPRING_DATASOURCE_URL=$SERVER_DB_URL mvn spring-boot:run
```

**终端 2 — 配件服务**

```bash
cd dc-accessory-service
SPRING_DATASOURCE_URL=$ACCESSORY_DB_URL mvn spring-boot:run
```

**终端 3 — API 网关**

```bash
cd dc-gateway
mvn spring-boot:run
```

> 等待 Nacos 注册完成（约 15~30 秒）后再访问 API。

### 第七步：启动前端

**终端 4**

```bash
cd frontend
npm run dev
```

前端 Vite 开发服务器监听 `0.0.0.0:3000`，Cursor 会自动转发端口到宿主机。

### 第八步：访问系统

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端（Cursor 端口转发） |
| http://localhost:8080/api/servers | API 网关 |
| http://localhost:8848/nacos | Nacos 控制台 |
| localhost:5432 | PostgreSQL（可用 GUI 客户端连接） |

## 端口转发

`devcontainer.json` 已配置以下端口自动转发：

| 端口 | 服务 | 转发行为 |
|------|------|----------|
| 3000 | 前端 Vite | 自动通知 |
| 8080 | API 网关 | 静默转发 |
| 8081 | 服务器服务 | 静默转发 |
| 8082 | 配件服务 | 静默转发 |
| 8848 | Nacos | 静默转发 |
| 5432 | PostgreSQL | 静默转发 |

若端口转发未自动生效，在 Cursor 底部面板打开 **Ports** 标签，手动添加转发规则。

## 推荐 Cursor 扩展

`devcontainer.json` 已预装以下扩展（进入容器后自动安装）：

| 扩展 | 用途 |
|------|------|
| Extension Pack for Java | Java 语言支持、调试、Maven |
| Spring Boot Extension Pack | Spring Boot 开发与运行 |
| Vue - Official (Volar) | Vue 3 语法高亮与智能提示 |
| Docker | 容器管理 |
| ESLint | 前端代码检查 |

## 开发工作流

### 修改后端代码

1. 在 Cursor 中编辑 Java 文件
2. 保存后重启对应的 `mvn spring-boot:run` 进程

### 修改前端代码

1. 编辑 `.vue` 文件
2. Vite 热更新自动刷新浏览器，无需重启

### 修改数据库

- 数据持久化在 Docker 卷 `postgres_data` 中
- 重置数据：`docker compose down -v`（在容器内或宿主机执行均可）
- 重新启动微服务后 `DataInitializer` 会检测空表并写入示例数据

### 连接数据库（GUI 客户端）

在宿主机使用 DBeaver / DataGrip 等工具：

| 参数 | 值 |
|------|-----|
| Host | `localhost` |
| Port | `5432` |
| Database | `dc_server_db` 或 `dc_accessory_db` |
| Username | `dcasset` |
| Password | `dcasset123` |

## 退出开发容器

1. `Ctrl+Shift+P` → **Dev Containers: Reopen Folder Locally**
2. Cursor 回到宿主机模式，基础设施容器保持运行
3. 完全停止：`docker compose down`

## 常见问题

| 现象 | 原因 | 解决 |
|------|------|------|
| Reopen in Container 失败 | Docker 未运行或权限不足 | 确认 `docker ps` 正常；检查用户是否在 `docker` 组 |
| 构建 devcontainer 超时 | 网络慢，拉取镜像失败 | 配置 Docker 镜像加速；重试 Reopen in Container |
| 微服务连不上数据库 | PostgreSQL 未就绪 | `docker compose ps` 确认 postgres 为 healthy |
| 网关 503 | 微服务未注册到 Nacos | 等待 30 秒；确认 `NACOS_SERVER_ADDR=nacos:8848` |
| 前端 API 请求失败 | 网关未启动或端口未转发 | 确认 8080 端口转发；检查 vite proxy 配置 |
| `mvn` 命令找不到 | 未在 devcontainer 内 | 确认左下角显示容器名称，而非本地 |
| 源码修改不生效（后端） | 需重启 Spring Boot | 停止并重新 `mvn spring-boot:run` |

## 与生产部署的切换

| 场景 | 命令 |
|------|------|
| 远程开发 | Cursor → Reopen in Container |
| 生产部署 | `docker compose up -d --build` |
| 仅启动基础设施（不用 Cursor） | `docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d postgres nacos` |

## 数据库变更说明

项目已从 H2 内存库迁移至 PostgreSQL：

- 开发容器中通过 `postgres` 主机名连接数据库（Docker 内部 DNS）
- 宿主机本地开发（不用容器）时，默认连接 `localhost:5432`
- 业务代码无需修改，仅数据源配置变更

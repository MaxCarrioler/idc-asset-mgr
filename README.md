# IDC基础设施管理系统 Demo

基于 **PigX 4.1.0 开源版**微服务架构的 IDC 基础设施管理系统，以**服务器**为核心管理单位，支持 7 类配件 CRUD、操作记录追踪、元数据自定义管理、硬件资源汇总统计、独立备件库存，以及**配件中心**调取审批/出库流转。权限采用 PigX 生产级 RBAC（OAuth2 + `sys_role`/`sys_menu`）。

## 系统架构

```
┌─────────────┐     ┌──────────────┐     ┌──────────────────────┐
│  Vue3 前端   │────▶│ pig-gateway  │────▶│  pig-dc-server-biz   │
│  :3000      │     │   :9999      │     │       :8081          │
└─────────────┘     └──────┬───────┘     └──────────┬───────────┘
                           │                        │
              ┌────────────┼────────────┐  ┌──────────────────────────┐
              ▼            ▼            ▼  │  pig-dc-accessory-biz    │
        ┌──────────┐ ┌──────────┐ ┌──────────┐│        :8082             │
        │ pig-auth │ │pig-upms  │ │ accessory│└──────────────────────────┘
        │  :3001   │ │  :4000   │ │  center  │
        └────┬─────┘ └────┬─────┘ └──────────┘
             │            │
             ▼            ▼
        ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
        │  Redis   │ │  MySQL   │ │PostgreSQL│ │  Nacos   │
        │  :6379   │ │  :3306   │ │  :5432   │ │  :8848   │
        └──────────┘ └──────────┘ └──────────┘ └──────────┘
```

> PigX 平台代码位于同级目录 `../pig`（4.1.0），本项目通过符号链接引用 `pig-gateway` 等模块。IDC 业务代码在 `pig-dc/` 下。身份权限在 MySQL（`pig` 库），业务数据在 PostgreSQL。

## 模块说明

| 模块 | 端口 | 说明 |
|------|------|------|
| PostgreSQL | 5432 | 业务数据（`dc_server_db` / `dc_accessory_db`） |
| MySQL | 3306 | PigX RBAC（`sys_user` / `sys_role` / `sys_menu` / OAuth 客户端） |
| Nacos | 8848 | 服务注册与发现 |
| Redis | 6379 | OAuth opaque token、网关缓存 |
| pig-auth | 3001 | OAuth2 授权服务器（经网关 `/auth/**`） |
| pig-upms-biz | 4000 | 用户权限管理（经网关 `/admin/**`） |
| pig-gateway | 9999 | PigX API 网关 + IDC 路由转发 |
| pig-dc-server-biz | 8081 | 服务器管理、操作记录、服务器类型与 CPU 架构元数据 |
| pig-dc-accessory-biz | 8082 | 配件管理、备件库存、**配件中心流转** |
| pig-dc-common | - | 公共 DTO / 统一响应封装 |
| frontend | 3000 | Vue3 + Element Plus 前端（需登录） |

## 角色与权限

| 角色码 | 名称 | 演示账号 | 权限概要 |
|--------|------|----------|----------|
| `ROLE_ADMIN` | 全局管理员 | `admin` / `123456` | 全部权限；**用户审批 / 注销 / 角色授权** |
| `ROLE_ACCESSORY_ADMIN` | 配件管理员 | `accessory_admin` / `123456` | 配件中心审批、驳回、出库 |
| `GENERAL_USER` | 普通用户 | `user` / `123456` | 提交调取申请、查看本人申请 |

权限点：`accessory:transfer:view` / `apply` / `approve` / `reject` / `outbound`。

用户中心（仅超管）：`sys_user_view` / `sys_user_edit` / `sys_user_del`、`sys_role_view` / `sys_role_*`。

### 注册与审批

1. 访客打开 `/register` 提交注册 → 账号创建为**待审批**（`lock_flag=9`），不可登录。
2. 超级管理员在 **用户中心 → 用户管理** 筛选「待审批」→ **通过**（解锁）或 **驳回**（软删除）。
3. 审批通过后默认为普通用户（`GENERAL_USER`），可登录；超管可再分配配件管理员等业务角色。
4. **超管直接建号**：用户中心「新建用户」可指定用户名、密码、昵称、业务角色，默认立即生效（无需审批）；编辑用户时可重置密码。
5. **注销**：仅超管可从系统删除用户；不可删除最后一个超级管理员；业务侧不可创建/删除/篡改 `ROLE_ADMIN`。

## 前端功能

### 主导航

| 导航 | 说明 |
|------|------|
| 服务器列表 | 服务器分页列表、搜索、新增/编辑/删除 |
| 数据统计 | Region / 服务器类型 / CPU 架构多维度筛选，硬件资源汇总表格 |
| 元数据管理 | 服务器类型、CPU 架构等元数据自定义增删改查 |
| 备件统计 | 按规格种类汇总备件数量，支持种类与明细 CRUD |
| 配件中心 | 调取申请 → 选定序列号审批 → 出库（来源服务器下拉：真实序列号或「无」）→ 流转记录（现役服务器） |
| 用户中心 | （仅超管）页内 Tab：用户管理（审批/注销/赋角色）、角色管理（CRUD/授权） |

### 元数据管理

- **服务器类型**：自定义新建、编辑、删除，持久化至 PostgreSQL
  - 内置类型：**COMPUTE（计算节点）**、**STORAGE（存储节点）**，不可删除
  - 可配置是否统计 CPU 线程、是否汇总硬盘容量及规格
  - 删除限制：内置类型不可删；仍有服务器绑定的类型不可删
- **CPU 架构**：自定义新建、编辑、删除
  - 内置架构：**X86_64**、**ARM64**，不可删除
  - 供服务器录入与数据统计筛选使用

### 服务器列表

列表字段：ID、服务器名称、Region 名称、序列号、机架位置、型号、CPU 架构、厂商、状态、操作系统、更新时间、操作记录。

- 点击服务器名称 → 配件详情页
- 点击「查看记录」→ 操作记录页
- 编辑时可填写**变更原因**，系统自动写入操作记录

### 数据统计

- **默认**：三组筛选均为空，展示全量服务器硬件资源汇总
- **筛选**：支持 Region 区域、服务器类型（ID）、CPU 架构多选，变更后实时重新聚合
- **主表格**：固定表头展示 CPU 核心总数、线程总数、内存总容量、各规格硬盘物理总容量（1.8T / 2.4T / 3.84T / 4T / 8T）、硬盘物理总容量
- **筛选数据源**：`/api/servers/regions`、`/api/server-types`、`/api/cpu-architectures`

### 备件统计

备件库存与服务器配件独立管理，用于库房/冷备件台账。

- **统计概览**：每种规格单独一类（如「1.8T 硬盘」「8T 硬盘」），卡片展示该种类备件数量
- **种类管理**：备件种类自定义增删改查；内置种类不可删除，有备件记录时不可删除
- **备件明细**（二级页面）：一件备件一条记录，以**序列号**唯一标识
  - 字段：序列号（必填）、厂商/品牌、规格摘要、存放位置、备注
  - 同一种类下序列号不可重复
- **内置备件种类示例**：1.8T 硬盘、8T 硬盘、4T 硬盘、Intel Xeon Gold 6330、32GB DDR4 内存、10G 四口网卡、1400W 电源

### 配件详情（二级页面）

7 个 Tab：CPU、内存、硬盘、网卡、HBA 卡、RAID/SAS 卡、电源模块。每类配件支持新增、编辑、删除。

## 环境要求

### Docker 部署

| 组件 | 版本 |
|------|------|
| Docker | 24.0+ |
| Docker Compose | v2.20+ |
| 可用内存 | 4 GB+（推荐 8 GB） |

### 本地开发（非容器）

| 组件 | 版本 | 说明 |
|------|------|------|
| **JDK** | **17**（必须） | Java 25 与 Lombok 不兼容，编译会失败 |
| Maven | 3.8+ | 后端编译与 `spring-boot:run` |
| Node.js | 20+ | 前端 Vite 开发服务器 |
| PostgreSQL | 18.x | 两个微服务各用独立业务库 |
| MySQL | 8.0 | PigX 身份权限库 `pig`（用户/角色/菜单/OAuth） |
| Nacos | 2.4.3 | standalone 模式（服务发现） |
| Redis | 7.x | OAuth token / 网关；`start-local.sh` 可自动启动用户目录 Redis |
| PigX 源码 | 4.1.0 | 克隆到 `../pig`，与 IDC-Manager 同级目录 |

---

## 快速启动

### 方式 A：Docker 一键部署（推荐用于演示/部署）

```bash
# 首次：安装 Docker 并加入 docker 组
sudo scripts/install-docker.sh
# 重新登录后执行

docker compose up -d --build
```

首次构建约 5~15 分钟。启动完成后访问 **http://localhost:3000**。

```bash
docker compose ps          # 查看状态
docker compose logs -f     # 查看日志
docker compose down        # 停止
docker compose down -v     # 停止并清空数据库
```

详细说明见 [DEPLOYMENT.md](DEPLOYMENT.md)。

### 方式 B：本地单机启动（推荐用于开发调试）

#### 0. 准备 PigX 平台源码

```bash
cd ..
# 推荐使用本项目配套 fork（含用户中心等 IDC 补丁）
git clone -b idc-v1.0.0 https://github.com/MaxCarrioler/pig.git
# 或上游原版：git clone -b 4.1.0 https://github.com/pig-mesh/pig.git
# 或解压 PigX 4.1.0 到 ../pig
```

#### 1. 准备基础设施

**PostgreSQL** — 创建用户与数据库：

```bash
# 示例（按实际安装方式调整）
sudo -u postgres psql <<'EOF'
CREATE USER dcasset WITH PASSWORD 'dcasset123' CREATEDB;
CREATE DATABASE dc_server_db OWNER dcasset;
CREATE DATABASE dc_accessory_db OWNER dcasset;
EOF
```

默认连接配置（可通过环境变量 `SPRING_DATASOURCE_URL` 覆盖）：

| 微服务 | 数据库 | 默认地址 |
|--------|--------|----------|
| pig-dc-server-biz | `dc_server_db` | `jdbc:postgresql://192.168.59.101:5432/dc_server_db` |
| pig-dc-accessory-biz | `dc_accessory_db` | `jdbc:postgresql://192.168.59.101:5432/dc_accessory_db` |

凭据：用户名 `dcasset`，密码 `dcasset123`。

**MySQL 8** — PigX RBAC（本地推荐，可不依赖 Docker）：

```bash
# 若尚未有 MySQL，可用 micromamba 安装到项目目录（已验证可用）
# micromamba create -y -p .local/mysql-env -c conda-forge mysql-server=8.4.2 mysql-client=8.4.2
# 首次导入：
#   .local/mysql-env/bin/mysql -h127.0.0.1 -uroot -proot < db-pig/pig.sql
#   .local/mysql-env/bin/mysql -h127.0.0.1 -uroot -proot < db-pig/pig_config.sql
#   .local/mysql-env/bin/mysql -h127.0.0.1 -uroot -proot < docker/mysql-init/02-gateway-idc-routes.sql
#   .local/mysql-env/bin/mysql -h127.0.0.1 -uroot -proot < docker/mysql-init/02-accessory-center-rbac.sql
#   .local/mysql-env/bin/mysql -h127.0.0.1 -uroot -proot < docker/mysql-init/03-user-center-rbac.sql
# 之后 start-local.sh 会自动检测/拉起 .local/mysql-env
```

默认：`root`/`root`，库名 `pig`（RBAC）和 `pig_config`（Nacos），端口 `3306`。演示账号见上文「角色与权限」。

**Nacos 2.4.3** — standalone 模式启动：

```bash
# 下载解压 nacos-server-2.4.3.tar.gz 后
bin/startup.sh -m standalone
```

验证：http://localhost:8848/nacos

#### 2. 编译项目

```bash
# 必须使用 JDK 17
export JAVA_HOME=/path/to/jdk-17   # 例如 ~/.local/opt/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"

mvn clean install -DskipTests \
  -pl pig-auth,pig-upms/pig-upms-biz,pig-dc/pig-dc-server-biz,pig-dc/pig-dc-accessory-biz,pig-gateway -am
cd frontend && npm install
```

#### 3. 一键启动（推荐）

```bash
scripts/start-local.sh
```

脚本会检查 PostgreSQL / MySQL / Nacos / Redis，并依次启动 `pig-auth`（:3001）、`pig-upms-biz`（:4000）、`pig-dc-server-biz`、`pig-dc-accessory-biz`、`pig-gateway`（:9999）与前端。

停止服务：`scripts/stop-local.sh`

浏览器访问：**http://localhost:3000**，使用演示账号登录；也可从登录页进入 **去注册**，由 `admin` 在用户中心审批。

> 业务库首次由 Hibernate `ddl-auto: update` 建表，`DataInitializer` 写入示例数据并回填备件 `status`/来源字段。

### 混合模式（开发推荐）

仅用 Docker 跑基础设施，微服务和前端在本机运行：

```bash
docker compose up -d postgres mysql redis nacos
scripts/start-local.sh
```
---

## 常见问题

| 现象 | 处理方式 |
|------|----------|
| `mvn` 编译报 Lombok 相关错误 | 确认使用 **JDK 17**，不要用 Java 25 |
| 数据库连接失败 | 检查 PostgreSQL / MySQL 是否运行、地址/用户/库是否正确 |
| 登录失败 / 401 | 确认 `pig-auth`、MySQL、Redis 已启动；密码经 AES 加密后提交（前端已处理） |
| 接口 403 | 当前角色无对应权限点（如普通用户不可审批出库） |
| 网关返回 503 | Nacos 未启动或微服务尚未注册，等待 15~30 秒后重试 |
| 网关启动失败 | 确认 Redis 已运行（`start-local.sh` 会尝试自动启动本地 Redis） |
| 未安装 Docker | 运行 `sudo scripts/install-docker.sh` 后重新登录 |
| 前端无法访问 | Docker 方式：确认 `dc-frontend` 为 running；本地方式：确认 `npm run dev` 已启动 |
| 端口冲突 | Docker：修改 `docker-compose.yml` 端口映射；本地：检查占用进程 |

## 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端（需登录） |
| http://localhost:9999/auth/oauth2/token | OAuth2 令牌（经网关） |
| http://localhost:9999/api/accessory-center/requests | 配件中心申请列表 |
| http://localhost:9999/api/servers | 服务器 API（经 PigX 网关） |
| http://localhost:9999/api/server-types | 服务器类型 API |
| http://localhost:9999/api/cpu-architectures | CPU 架构元数据 API |
| http://localhost:9999/api/statistics/hardware | 硬件资源统计（全量） |
| http://localhost:9999/api/statistics/hardware?regions=华北-北京&serverTypeIds=1,2&cpuArchitectures=X86_64 | 多维度筛选统计 |
| http://localhost:9999/api/spare-part-categories | 备件种类 CRUD |
| http://localhost:9999/api/spare-parts/summary | 备件库存汇总 |
| http://localhost:9999/api/spare-parts/items?categoryId=1 | 按种类查询备件明细 |
| http://localhost:9999/api/servers/regions | 全量 Region 列表 |
| http://localhost:9999/api/accessories/cpus?serverId=1 | CPU 配件 API |
| http://localhost:8081/swagger-ui.html | 服务器服务 Swagger |
| http://localhost:8082/swagger-ui.html | 配件服务 Swagger |
| http://localhost:8848/nacos | Nacos 控制台（默认无鉴权） |

## 启动脚本说明

| 脚本 | 说明 |
|------|------|
| `scripts/install-docker.sh` | 安装 Docker 与 Compose（需 sudo） |
| `scripts/start.sh` | Docker 方式：构建并启动全部容器 |
| `scripts/start-local.sh` | 本地方式：JDK 17 一键启动全部服务 |
| `scripts/stop-local.sh` | 本地方式：停止全部服务 |
| `scripts/install-local-env.sh` | 本地方式：用户目录安装 Maven / Node / PostgreSQL / Nacos |
| `scripts/dev-env.sh` | Dev Container 内加载开发环境变量 |

## 前端文件结构

```
frontend/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.js
    ├── App.vue
    ├── api/index.js              # Axios + Bearer 令牌
    ├── config/accessory.js
    ├── directives/permission.js  # v-permission
    ├── stores/auth.js            # 登录态
    ├── router/index.js           # 路由守卫
    ├── layouts/MainLayout.vue
    ├── views/
    │   ├── Login.vue
    │   ├── AccessoryCenter.vue   # 配件中心
    │   ├── ServerList.vue
    │   ├── Statistics.vue
    │   ├── MetadataManagement.vue
    │   ├── SparePartsStats.vue
    │   ├── SparePartDetail.vue
    │   ├── AccessoryDetail.vue
    │   └── OperationRecords.vue
    └── components/
        ├── ServerFormDialog.vue
        ├── ServerTypeFormDialog.vue
        ├── CpuArchitectureFormDialog.vue
        ├── SparePartCategoryFormDialog.vue
        ├── SparePartFormDialog.vue
        └── AccessoryFormDialog.vue
```

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编译与运行必须；与 Lombok 不兼容 Java 25 |
| PigX | 4.1.0 | 开源版微服务底座（网关 / 公共组件等） |
| Spring Boot | 4.0.7 | 随 PigX parent |
| Spring Cloud | 2025.1.2 | 随 PigX parent |
| Spring Cloud Alibaba | 2025.1.0.0 | Nacos 服务发现 |
| Spring Cloud Gateway | — | PigX `pig-gateway`（端口 9999） |
| Spring Data JPA / Hibernate | — | ORM，`ddl-auto: update` 自动建表 |
| SpringDoc OpenAPI | 3.0.3 | Swagger UI（经 `pig-common-swagger`） |
| Lombok | 1.18.46 | 随 PigX |
| PostgreSQL JDBC | — | 业务库驱动 |
| Maven | 3.9+ | 构建；Docker 镜像 `maven:3.9-eclipse-temurin-17` |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | `^3.5.13` |
| Element Plus | 2.x | `^2.9.1`（当前安装约 2.14） |
| Vite | 6.x | `^6.0.5` |
| Vue Router | 4.x | `^4.5.0` |
| Axios | 1.x | `^1.7.9` |
| Node.js | 20+ | 开发 / 构建；Docker 用 `node:20-alpine` |

### 基础设施

| 组件 | 版本 | Docker 镜像 / 运行时 |
|------|------|----------------------|
| PostgreSQL | 18.x | `postgres:18-alpine` |
| Redis | 7.x | `redis:7-alpine`（本地约 7.2） |
| Nacos | 2.4.3 | `nacos/nacos-server:v2.4.3`（standalone） |
| 后端运行时 | JRE 17 | `eclipse-temurin:17-jre-alpine` |
| 前端生产托管 | Nginx 1.27 | `nginx:1.27-alpine`（多阶段构建） |

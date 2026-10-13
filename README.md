# IDC基础设施管理系统

轻量单体架构的 IDC 基础设施管理系统，以**服务器**为核心管理单位，支持 7 类配件 CRUD、操作记录追踪、元数据自定义管理、硬件资源汇总统计、独立备件库存，以及**配件中心**调取审批/出库流转。权限采用内置 RBAC（JWT + `sys_user`/`sys_role`/`sys_menu`）。

## 系统架构

```
┌─────────────┐     ┌────────────────────────────┐     ┌──────────────┐
│ React 前端   │────▶│          idc-app           │────▶│ PostgreSQL   │
│   :3000     │     │ Spring Boot 单体 :8080      │     │   :5433      │
└─────────────┘     │ 认证 / 用户中心 / 服务器 /   │     │ idc_manager  │
                    │ 配件 / 备件 / 配件中心        │     └──────────────┘
                    └────────────────────────────┘
```

单应用 `idc-app`（JDK 17 + Spring Boot 3.3 + Spring Security + JWT + JPA）+ PostgreSQL 单库 + React 18 前端，共 3 个容器/进程，无任何微服务中间件（无 Nacos / Redis / MySQL / 网关）。

## 模块说明

| 模块 | 端口 | 说明 |
|------|------|------|
| PostgreSQL | 5433 | 单库 `idc_manager`（业务 + RBAC 全部数据，JPA 自动建表） |
| idc-app（backend） | 8080 | 单体后端：认证（`/auth/**`）、用户中心（`/admin/**`）、业务（`/api/**`） |
| frontend | 3000 | React 18 + Ant Design 5 前端（需登录） |

后端包结构（`com.dcasset`）：

| 包 | 职责 |
|----|------|
| `common` | 统一响应 `Result` / `PageResult` / 公共 DTO / 全局异常 |
| `security` | JWT 签发与校验、Spring Security 配置、密码解码 |
| `admin` | RBAC 实体/仓库/服务（用户、角色、菜单）+ `/auth`、`/admin/**` 端点 |
| `server` | 服务器管理、操作记录、服务器类型与 CPU 架构元数据 |
| `accessory` | 配件管理、备件库存、配件中心流转、硬件/备件统计 |

## 角色与权限

| 角色码 | 名称 | 演示账号 | 权限概要 |
|--------|------|----------|----------|
| `ROLE_ADMIN` | 全局管理员 | `admin` / `123456` | 全部权限（`*:*:*`）；**用户审批 / 注销 / 角色授权**；可删除内置元数据与所有状态备件 |
| `ROLE_ACCESSORY_ADMIN` | 配件管理员 | `accessory_admin` / `123456` | 配件中心审批、驳回、出库；备件 CRUD 与种类管理 |
| `GENERAL_USER` | 普通用户 | `user` / `123456` | 提交调取申请、查看本人申请、查看备件（不可增删改） |

权限点：`accessory:transfer:view` / `apply` / `approve` / `reject` / `outbound`。

备件权限点：`spare_part:view` / `edit` / `delete` / `category_manage`（配件管理员与全局管理员均拥有）。

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
| 服务器统计 | 父菜单，含 5 个子菜单：服务器列表、数据统计、服务器类型、CPU 架构、服务器硬盘类型 |
| 备件统计 | 按配件类型分类汇总备件数量，支持种类与明细 CRUD；出库后与配件中心联动（在库减量 / 明细状态） |
| 配件中心 | 调取申请 → 选定序列号审批 → 出库（来源服务器：真实序列号或「无」）→ 流转记录（**现役服务器**）→ **导出 Excel** |
| 用户中心 | （仅超管）页内 Tab：用户管理（审批/注销/赋角色）、角色管理（CRUD/授权） |
| 注册页 | 访客自助注册（待审批，不可登录） |

### 元数据管理（服务器统计子菜单）

- **服务器类型**：自定义新建、编辑、删除，持久化至 PostgreSQL
  - 内置类型：**COMPUTE（计算节点）**、**STORAGE（存储节点）**
  - 可配置是否统计 CPU 线程、是否汇总硬盘容量及规格
  - 删除限制：全局管理员可删除内置类型；仍有服务器绑定的类型不可删
- **CPU 架构**：自定义新建、编辑、删除
  - 内置架构：**X86_64**、**ARM64**
  - 供服务器录入与数据统计筛选使用
  - 删除限制：全局管理员可删除内置架构
- **服务器硬盘类型**：自定义新建、编辑、删除
  - 内置类型：**SAS**、**SATA**、**SSD**
  - 供硬盘配件录入、备件分类与数据统计筛选使用
  - 类型编码在统计筛选中同时比对硬盘的介质类型（`diskType`）与接口类型（`interfaceType`）
  - 删除限制：全局管理员可删除内置类型；仍有硬盘配件引用该类型编码时不可删

### 服务器列表（服务器统计子菜单）

列表字段：ID、服务器名称、服务器类型、Region 名称、序列号、机架位置、型号、CPU 架构、厂商、状态、操作系统、更新时间、操作记录。

- 服务器类型列显示计算节点 / 存储节点（取自服务器类型元数据）
- 点击服务器名称 → 配件详情页
- 点击「查看记录」→ 操作记录页
- 编辑时可填写**变更原因**，系统自动写入操作记录
- 变更服务器名称时，级联更新所有关联记录中的冗余名称副本（调取申请、出库记录、备件来源/现役服务器）；应用启动时自动同步一次，修复历史旧数据

### 数据统计（服务器统计子菜单）

- **默认**：四组筛选均为空，展示全量服务器硬件资源汇总
- **筛选**：支持 Region 区域、服务器类型（ID）、CPU 架构、硬盘类型多选，变更后实时重新聚合
- **硬盘类型筛选口径**：硬盘类型属于配件级属性，类型编码**同时比对硬盘的介质类型（`diskType`，如 SSD / HDD）与接口类型（`interfaceType`，如 SAS / SATA / NVMe）**，任一字段命中即视为匹配（兼容 SAS / SATA / SSD 这类跨口径类型定义）；服务器下只要存在任一块匹配硬盘即命中筛选，命中后聚合该服务器全量硬件资源；选项数据源为元数据管理中的硬盘类型（内置 SAS / SATA / SSD）
- **主表格**：固定表头展示 CPU 核心总数、线程总数、内存总容量、各规格硬盘物理总容量（1.8T / 2.4T / 3.84T / 4T / 8T）、硬盘物理总容量
- **筛选数据源**：`/api/servers/regions`、`/api/server-types`、`/api/cpu-architectures`、`/api/hard-disk-types`

### 备件统计

备件库存与服务器配件独立管理，用于库房/冷备件台账。

- **分类列表**：按配件类型（CPU、内存、硬盘等）分类展示备件种类，点击进入该类备件明细
- **种类管理**：备件种类自定义增删改查；全局管理员可删除内置种类，有备件记录时不可删除
- **备件明细**（二级页面）：一件备件一条记录，以**序列号**唯一标识
  - 字段：序列号（必填）、厂商/品牌、规格摘要、存放位置、备注
  - 同一种类下序列号不可重复
- **删除权限**：全局管理员可删除所有状态的备件（含已出库 / 已预留），其他角色不可删除已出库或已预留备件
- **内置备件种类示例**：1.8T 硬盘、8T 硬盘、4T 硬盘、Intel Xeon Gold 6330、32GB DDR4 内存、10G 四口网卡、1400W 电源
- **与配件中心联动**：出库时对选中备件执行 `OUT`、在库数量减量，明细状态变为「已出库」；流转记录中的「现役服务器」取自最近一次出库目标

### 配件中心

页内 Tab：调取申请、待审批、流转记录。

- **申请**：普通用户提交调取申请并选择备件
- **审批 / 出库**：配件管理员选定序列号后审批；出库时为每个备件选择来源服务器（真实序列号或「无」）
- **删除申请**：全局管理员可删除调取申请记录（已出库状态除外）；删除已审批申请时自动释放已预留备件
- **流转记录**：展示来源服务器与现役服务器；支持 **导出 Excel**（`xlsx`）

### 配件详情（二级页面）

7 个 Tab：CPU、内存、硬盘、网卡、HBA 卡、RAID/SAS 卡、电源模块。每类配件支持新增、编辑、删除。

## 环境要求

### Docker 部署

| 组件 | 版本 |
|------|------|
| Docker | 24.0+ |
| Docker Compose | v2.20+ |
| 可用内存 | 2 GB+ |

### 本地开发（非容器）

| 组件 | 版本 | 说明 |
|------|------|------|
| **JDK** | **17**（必须） | Java 25 与 Lombok 不兼容，编译会失败 |
| Maven | 3.8+ | 后端编译与打包 |
| Node.js | 20+ | 前端 Vite 开发服务器 |
| PostgreSQL | 15+ | 单库 `idc_manager`，表结构与种子数据由应用自动初始化 |

---

## 快速启动

### 方式 A：Docker 一键部署（推荐用于演示/部署）

```bash
cd IDC-Manager
docker compose up -d --build
```

首次构建约 3~8 分钟（下载 Maven / Node 依赖并编译）。启动完成后访问 **http://localhost:3000**。

```bash
docker compose ps          # 查看状态
docker compose logs -f     # 查看日志
docker compose down        # 停止
docker compose down -v     # 停止并清空数据库
```

仅 3 个容器：`dc-postgres`（:5433）、`dc-backend`（:8080）、`dc-frontend`（:3000）。详细说明见 [DEPLOYMENT.md](DEPLOYMENT.md)。

### 方式 B：本地单机启动（推荐用于开发调试）

#### 1. 准备 PostgreSQL

默认连接 `192.168.59.101:5432`，凭据 `dcasset` / `dcasset123`（可用环境变量 `DB_HOST` / `DB_PORT` / `DB_USER` / `DB_PASSWORD` 覆盖）。启动脚本会自动创建 `idc_manager` 库（需 `psql` 可用）：

```bash
# 手动建库示例
sudo -u postgres psql <<'EOF'
CREATE USER dcasset WITH PASSWORD 'dcasset123' CREATEDB;
CREATE DATABASE idc_manager OWNER dcasset;
EOF
```

#### 2. 编译与启动

```bash
# 必须使用 JDK 17
export JAVA_HOME=/path/to/jdk-17   # 例如 ~/.local/opt/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"

mvn clean package -DskipTests
cd frontend && npm install && cd ..

scripts/start-local.sh
```

脚本会检查 PostgreSQL、自动建库、启动单体后端（:8080）与前端（:3000）。

停止服务：`scripts/stop-local.sh`

浏览器访问：**http://localhost:3000**，使用演示账号登录；也可从登录页进入 **去注册**，由 `admin` 在用户中心审批。

> 首次启动由 Hibernate `ddl-auto: update` 建表，`DataInitializer` 写入示例业务数据；`RbacDataInitializer` 幂等写入三个演示账号、三个角色与全部权限点菜单。

## 常见问题

| 现象 | 处理方式 |
|------|----------|
| `mvn` 编译报 Lombok 相关错误 | 确认使用 **JDK 17**，不要用 Java 25 |
| 数据库连接失败 | 检查 PostgreSQL 是否运行、`DB_HOST`/`DB_USER`/`DB_NAME` 是否正确 |
| 登录失败 / 401 | 确认后端与 PostgreSQL 已启动；密码经 AES 加密后提交（前端已处理）；账号处于待审批（`lock_flag=9`）状态时不可登录 |
| 接口 403 | 当前角色无对应权限点（如普通用户不可审批出库） |
| 令牌过期 | 前端自动用 refresh_token 刷新；刷新也过期则重新登录 |
| 端口冲突 | Docker：修改 `docker-compose.yml` 端口映射；本地：检查 8080 / 3000 占用进程 |
| 前端无法访问 | Docker 方式：确认 `dc-frontend` 为 running；本地方式：确认 `npm run dev` 已启动 |

## 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:3000 | 前端（需登录） |
| http://localhost:8080/auth/oauth2/token | OAuth2 形态令牌端点（form 表单） |
| http://localhost:8080/api/servers | 服务器 API |
| http://localhost:8080/api/server-types | 服务器类型 API |
| http://localhost:8080/api/cpu-architectures | CPU 架构元数据 API |
| http://localhost:8080/api/hard-disk-types | 硬盘类型元数据 API |
| http://localhost:8080/api/statistics/hardware | 硬件资源统计（全量） |
| http://localhost:8080/api/statistics/hardware?regions=华北-北京&serverTypeIds=1,2&cpuArchitectures=X86_64&hardDiskTypes=SAS,SSD | 多维度筛选统计 |
| http://localhost:8080/api/spare-part-categories | 备件种类 CRUD |
| http://localhost:8080/api/spare-parts/by-type | 按配件类型汇总备件 |
| http://localhost:8080/api/spare-parts/items?categoryId=1 | 按种类查询备件明细 |
| http://localhost:8080/api/servers/regions | 全量 Region 列表 |
| http://localhost:8080/api/accessories/cpus?serverId=1 | CPU 配件 API |
| http://localhost:8080/api/accessory-center/requests | 配件中心申请列表 |
| http://localhost:8080/api/accessory-center/requests/{id} | 删除申请（仅全局管理员） |
| http://localhost:8080/swagger-ui.html | API 文档（Swagger） |

## 启动脚本说明

| 脚本 | 说明 |
|------|------|
| `scripts/install-docker.sh` | 安装 Docker 与 Compose（需 sudo） |
| `scripts/start.sh` | Docker 方式：构建并启动全部容器 |
| `scripts/start-local.sh` | 本地方式：JDK 17 一键启动后端 + 前端 |
| `scripts/stop-local.sh` | 本地方式：停止全部服务 |
| `scripts/install-local-env.sh` | 本地方式：用户目录安装 Maven / Node / PostgreSQL |

## 前端文件结构

```
frontend/
├── index.html
├── package.json
├── vite.config.js
├── .npmrc                          # npm 镜像源配置
└── src/
    ├── main.jsx                    # 入口（ConfigProvider + AuthProvider）
    ├── App.jsx                     # RouterProvider
    ├── api/index.js                # Axios + Bearer 令牌 + 401 自动刷新
    ├── api/userCenter.js           # 用户中心 API
    ├── config/accessory.js         # 配件类型配置
    ├── utils/format.js             # 格式化工具
    ├── stores/AuthContext.jsx       # React Context 登录态 + 权限
    ├── router/index.jsx            # React Router v6 + ProtectedRoute
    ├── components/Permission.jsx   # 权限包裹组件（替代 v-permission）
    ├── layouts/MainLayout.jsx      # antd Layout 主布局
    ├── views/
    │   ├── Login.jsx
    │   ├── Register.jsx            # 访客注册（待审批）
    │   ├── UserCenter.jsx          # 用户 / 角色管理（超管）
    │   ├── AccessoryCenter.jsx     # 配件中心（含 Excel 导出）
    │   ├── ServerList.jsx
    │   ├── Statistics.jsx
    │   ├── MetadataManagement.jsx
    │   ├── SparePartCategoryList.jsx  # 备件分类列表（按配件类型）
    │   ├── SparePartTypeList.jsx      # 某类型下备件种类列表
    │   ├── SparePartDetail.jsx
    │   ├── AccessoryDetail.jsx
    │   └── OperationRecords.jsx
    └── components/
        ├── ServerFormDialog.jsx
        ├── ServerTypeFormDialog.jsx
        ├── CpuArchitectureFormDialog.jsx
        ├── HardDiskTypeFormDialog.jsx
        ├── SparePartCategoryFormDialog.jsx
        ├── SparePartFormDialog.jsx
        └── AccessoryFormDialog.jsx
```

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编译与运行必须；与 Lombok 不兼容的高版本不可用 |
| Spring Boot | 3.3.5 | 单体应用 |
| Spring Security | 6.x | JWT（HMAC-SHA256）资源保护 + `@PreAuthorize` 方法级权限 |
| Spring Data JPA / Hibernate | 6.x | ORM，`ddl-auto: update` 自动建表 |
| SpringDoc OpenAPI | 2.x | Swagger UI |
| Lombok | 1.18.x | 代码简化 |
| PostgreSQL JDBC | — | 唯一数据源驱动 |
| Maven | 3.9+ | 构建；Docker 镜像 `maven:3.9-eclipse-temurin-17` |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| React | 18.x | `^18.3.1` |
| Ant Design | 5.x | `^5.22.0` |
| Vite | 6.x | `^6.0.5`（`@vitejs/plugin-react`） |
| React Router | 6.x | `^6.28.0`（createBrowserRouter + lazy） |
| Axios | 1.x | `^1.7.9` |
| Node.js | 20+ | 开发 / 构建；Docker 用 `node:20-alpine` |

### 基础设施

| 组件 | 版本 | Docker 镜像 / 运行时 |
|------|------|----------------------|
| PostgreSQL | 16.x | `postgres:16-alpine` |
| 后端运行时 | JRE 17 | `eclipse-temurin:17-jre-alpine` |
| 前端生产托管 | Nginx 1.27 | `nginx:1.27-alpine`（多阶段构建） |

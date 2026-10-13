# 2026-10-13 RBAC 权限体系完善与菜单结构优化

## 权限模型

- 备件模块新增 4 个权限点：`spare_part:view` / `spare_part:edit` / `spare_part:delete` / `spare_part:category_manage`
- 配件管理员（`ROLE_ACCESSORY_ADMIN`）拥有全部备件 CRUD 与种类管理权限
- 普通用户（`GENERAL_USER`）仅拥有备件查看权限，不可增删改
- 全局管理员（`ROLE_ADMIN`）拥有全部权限（`*:*:*`）

## 菜单结构

- 原「服务器列表」「数据统计」「元数据管理」三个顶级菜单合并为「服务器统计」父菜单，下设 5 个子菜单：服务器列表、数据统计、服务器类型、CPU 架构、服务器硬盘类型
- 侧栏「硬盘类型」更名为「服务器硬盘类型」
- 备件统计侧栏增加权限校验，无权限角色不可见

## 元数据管理

- 全局管理员可删除内置服务器类型（COMPUTE / STORAGE）、CPU 架构（X86_64 / ARM64）、硬盘类型（SAS / SATA / SSD）
- 原有业务约束保留：仍有服务器绑定或硬盘引用时不可删除

## 备件管理

- 删除「统计概览」页面（`SparePartsStats.jsx`），备件默认入口改为按配件类型分类列表
- 内置备件种类允许删除（有备件记录时仍不可删）
- 全局管理员可删除已出库（`OUT`）/ 已预留（`RESERVED`）状态的备件，其他角色不可
- 修复 `SparePartFormDialog` 闭包过期导致来源服务器解析失败的 Bug
- 修复 `SparePartTypeList` 仅有一个种类时 `categoryId` 未回退的 Bug

## 服务器列表

- 新增「服务器类型」列，显示计算节点 / 存储节点
- 编辑服务器名称时，级联更新所有关联记录中的冗余名称副本（调取申请目标服务器、出库来源/目标服务器、备件来源/现役服务器）
- 应用启动时自动执行一次名称同步（`backfillServerNames`），修复部署前已存在的旧名称数据

## 配件中心

- 全局管理员可删除调取申请记录（已出库状态除外）
- 删除申请时自动释放已预留的备件回「在库」状态

## 前端文件变更

| 变更 | 文件 |
|------|------|
| 新增 | `views/SparePartTypeList.jsx`、`views/SparePartCategoryList.jsx` |
| 删除 | `views/SparePartsStats.jsx` |
| 修改 | `layouts/MainLayout.jsx`、`router/index.jsx`、`views/ServerList.jsx`、`views/MetadataManagement.jsx`、`views/AccessoryCenter.jsx`、`views/SparePartDetail.jsx`、`components/SparePartFormDialog.jsx` |

## 后端文件变更

| 变更 | 文件 |
|------|------|
| 新增权限点 | `RbacDataInitializer.java`（ID 12000-12003） |
| 新增接口 | `AccessoryCenterController.java`（`DELETE /api/accessory-center/requests/{id}`） |
| 级联更新 | `server/ServerServiceImpl.java`（`cascadeUpdateServerName`） |
| 启动名称同步 | `server/DataInitializer.java`（`backfillServerNames`，一次性修复旧数据） |
| 权限增强 | `SparePartsController.java`、`SparePartCategoryController.java`（`@PreAuthorize`） |
| 删除限制调整 | `ServerTypeServiceImpl.java`、`CpuArchitectureServiceImpl.java`、`HardDiskTypeServiceImpl.java`、`SparePartCategoryServiceImpl.java`、`SparePartsServiceImpl.java` |

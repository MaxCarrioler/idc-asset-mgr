-- IDC 用户中心 RBAC 种子（可重复执行）
-- 菜单仅绑定超级管理员 ROLE_ADMIN (role_id=1)
USE `pig`;

-- 关闭注册短信校验（本地/演示）；生产可改为 '1'
INSERT INTO `sys_public_param` (`public_id`, `public_name`, `public_key`, `public_value`, `status`, `validate_code`,
	`create_by`, `update_by`, `create_time`, `update_time`, `public_type`, `system_flag`, `del_flag`)
VALUES (100, '注册短信校验开关', 'SITE_REGISTER_SMS_ENABLE', '0', '0', NULL,
	'admin', 'admin', NOW(), NOW(), '2', '1', '0')
ON DUPLICATE KEY UPDATE `public_value`=VALUES(`public_value`), `public_name`=VALUES(`public_name`);

-- 确保站点注册开启
UPDATE `sys_public_param`
SET `public_value` = '1'
WHERE `public_key` = 'SITE_REGISTER_ENABLE';

-- 用户中心菜单（前端路由）与按钮权限（复用 PigX sys_user_* / sys_role_*）
INSERT INTO `sys_menu` (`menu_id`, `name`, `permission`, `path`, `component`, `parent_id`, `icon`, `visible`, `sort_order`, `keep_alive`, `embedded`, `menu_type`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) VALUES
(11000, '用户中心', NULL, '/user-center', NULL, -1, 'iconfont icon-yonghuzhongxin', '1', 1, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(11001, '用户管理', NULL, '/user-center/users', NULL, 11000, NULL, '1', 1, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(11002, '角色管理', NULL, '/user-center/roles', NULL, 11000, NULL, '1', 2, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(11003, '用户查看', 'sys_user_view', NULL, NULL, 11001, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(11004, '用户编辑/审批', 'sys_user_edit', NULL, NULL, 11001, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(11005, '用户删除/驳回', 'sys_user_del', NULL, NULL, 11001, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(11006, '用户新增', 'sys_user_add', NULL, NULL, 11001, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(11007, '角色查看', 'sys_role_view', NULL, NULL, 11002, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(11008, '角色新增', 'sys_role_add', NULL, NULL, 11002, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(11009, '角色修改', 'sys_role_edit', NULL, NULL, 11002, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(11010, '角色删除', 'sys_role_del', NULL, NULL, 11002, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(11011, '角色授权', 'sys_role_perm', NULL, NULL, 11002, NULL, '1', 5, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0')
ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `permission`=VALUES(`permission`), `path`=VALUES(`path`), `parent_id`=VALUES(`parent_id`);

-- 仅超级管理员可见用户中心
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 11000),(1, 11001),(1, 11002),(1, 11003),(1, 11004),(1, 11005),(1, 11006),
(1, 11007),(1, 11008),(1, 11009),(1, 11010),(1, 11011);

-- 确保业务角色不拥有用户中心菜单（升级补丁）
DELETE FROM `sys_role_menu`
WHERE `menu_id` BETWEEN 11000 AND 11011
  AND `role_id` <> 1;

-- IDC 配件中心 RBAC 种子数据（在 pig.sql 之后执行）
USE `pig`;

-- 配件管理员角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_code`, `role_desc`, `create_by`, `update_by`, `create_time`, `update_time`, `del_flag`)
VALUES (3, '配件管理员', 'ROLE_ACCESSORY_ADMIN', '配件中心审批与出库', 'admin', 'admin', NOW(), NOW(), '0')
ON DUPLICATE KEY UPDATE `role_name`=VALUES(`role_name`), `role_code`=VALUES(`role_code`);

-- 配件中心菜单与按钮权限
INSERT INTO `sys_menu` (`menu_id`, `name`, `permission`, `path`, `component`, `parent_id`, `icon`, `visible`, `sort_order`, `keep_alive`, `embedded`, `menu_type`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`) VALUES
(10000, '配件中心', NULL, '/accessory-center', NULL, -1, 'iconfont icon-peijian', '1', 0, '0', '0', '0', 'admin', NOW(), 'admin', NOW(), '0'),
(10001, '查看申请', 'accessory:transfer:view', NULL, NULL, 10000, NULL, '1', 1, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(10002, '提交申请', 'accessory:transfer:apply', NULL, NULL, 10000, NULL, '1', 2, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(10003, '审批通过', 'accessory:transfer:approve', NULL, NULL, 10000, NULL, '1', 3, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(10004, '审批驳回', 'accessory:transfer:reject', NULL, NULL, 10000, NULL, '1', 4, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0'),
(10005, '配件出库', 'accessory:transfer:outbound', NULL, NULL, 10000, NULL, '1', 5, '0', NULL, '1', 'admin', NOW(), 'admin', NOW(), '0')
ON DUPLICATE KEY UPDATE `permission`=VALUES(`permission`), `name`=VALUES(`name`);

-- 全局管理员：全部配件中心权限
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 10000),(1, 10001),(1, 10002),(1, 10003),(1, 10004),(1, 10005);

-- 配件管理员：查看/审批/驳回/出库
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(3, 10000),(3, 10001),(3, 10003),(3, 10004),(3, 10005);

-- 普通用户：查看/提交申请
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2, 10000),(2, 10001),(2, 10002);

-- 示例用户（密码均为 123456，BCrypt）
-- $2a$10$c/Ae0pRjJtMZg3BnvVpO.eIK6WYWVbKTzqgdy3afR7w.vd.xi3Mgy == 123456
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `salt`, `phone`, `avatar`, `nickname`, `name`, `email`, `create_by`, `update_by`, `create_time`, `update_time`, `lock_flag`, `password_expire_flag`, `password_modify_time`, `del_flag`, `wx_openid`, `mini_openid`, `qq_openid`, `gitee_login`, `osc_id`, `wx_cp_userid`, `wx_ding_userid`, `dept_id`)
VALUES
(2, 'accessory_admin', '$2a$10$c/Ae0pRjJtMZg3BnvVpO.eIK6WYWVbKTzqgdy3afR7w.vd.xi3Mgy', '', '13800000002', NULL, '配件管理员', '配件管理员', 'accessory@idc.local', 'admin', 'admin', NOW(), NOW(), '0', '0', NULL, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1),
(3, 'user', '$2a$10$c/Ae0pRjJtMZg3BnvVpO.eIK6WYWVbKTzqgdy3afR7w.vd.xi3Mgy', '', '13800000003', NULL, '普通用户', '普通用户', 'user@idc.local', 'admin', 'admin', NOW(), NOW(), '0', '0', NULL, '0', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1)
ON DUPLICATE KEY UPDATE `password`=VALUES(`password`), `nickname`=VALUES(`nickname`);

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(2, 3),
(3, 2);

-- 便于 IDC 前端登录：关闭 test 客户端验证码（仍保留 AES 密码加密）
UPDATE `sys_oauth_client_details`
SET `additional_information` = '{"enc_flag":"1","captcha_flag":"0"}'
WHERE `client_id` = 'test';

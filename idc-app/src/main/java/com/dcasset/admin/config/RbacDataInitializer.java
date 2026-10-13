package com.dcasset.admin.config;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * RBAC 种子数据（幂等，原生 SQL 显式 ID + ON CONFLICT DO NOTHING）：
 * 三个演示账号、三个角色、配件中心与用户中心菜单权限。
 * 内容与原 docker/mysql-init/02-accessory-center-rbac.sql、03-user-center-rbac.sql 对齐。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RbacDataInitializer implements CommandLineRunner {

    /** 密码 123456 的 BCrypt 值 */
    private static final String DEMO_PASSWORD_BCRYPT =
            "$2a$10$c/Ae0pRjJtMZg3BnvVpO.eIK6WYWVbKTzqgdy3afR7w.vd.xi3Mgy";

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("初始化 RBAC 种子数据（角色 / 用户 / 菜单 / 授权，幂等）");
        LocalDateTime now = LocalDateTime.now();

        // 角色：1=超级管理员 2=普通用户 3=配件管理员
        insertRole(1L, "超级管理员", "ROLE_ADMIN", "系统整体管理", now);
        insertRole(2L, "普通用户", "GENERAL_USER", "查看与提交申请", now);
        insertRole(3L, "配件管理员", "ROLE_ACCESSORY_ADMIN", "配件中心审批与出库", now);

        // 演示用户（密码均为 123456）
        insertUser(1L, "admin", "超级管理员", "13800000001", "admin@idc.local", now);
        insertUser(2L, "accessory_admin", "配件管理员", "13800000002", "accessory@idc.local", now);
        insertUser(3L, "user", "普通用户", "13800000003", "user@idc.local", now);

        insertUserRole(1L, 1L);
        insertUserRole(2L, 3L);
        insertUserRole(3L, 2L);

        // 配件中心菜单与按钮权限
        insertMenu(10000L, "配件中心", null, "/accessory-center", -1L, "iconfont icon-peijian", 0, "0", now);
        insertMenu(10001L, "查看申请", "accessory:transfer:view", null, 10000L, null, 1, "1", now);
        insertMenu(10002L, "提交申请", "accessory:transfer:apply", null, 10000L, null, 2, "1", now);
        insertMenu(10003L, "审批通过", "accessory:transfer:approve", null, 10000L, null, 3, "1", now);
        insertMenu(10004L, "审批驳回", "accessory:transfer:reject", null, 10000L, null, 4, "1", now);
        insertMenu(10005L, "配件出库", "accessory:transfer:outbound", null, 10000L, null, 5, "1", now);

        // 用户中心菜单与按钮权限（仅超级管理员可见）
        insertMenu(11000L, "用户中心", null, "/user-center", -1L, "iconfont icon-yonghuzhongxin", 1, "0", now);
        insertMenu(11001L, "用户管理", null, "/user-center/users", 11000L, null, 1, "0", now);
        insertMenu(11002L, "角色管理", null, "/user-center/roles", 11000L, null, 2, "0", now);
        insertMenu(11003L, "用户查看", "sys_user_view", null, 11001L, null, 1, "1", now);
        insertMenu(11004L, "用户编辑/审批", "sys_user_edit", null, 11001L, null, 2, "1", now);
        insertMenu(11005L, "用户删除/驳回", "sys_user_del", null, 11001L, null, 3, "1", now);
        insertMenu(11006L, "用户新增", "sys_user_add", null, 11001L, null, 4, "1", now);
        insertMenu(11007L, "角色查看", "sys_role_view", null, 11002L, null, 1, "1", now);
        insertMenu(11008L, "角色新增", "sys_role_add", null, 11002L, null, 2, "1", now);
        insertMenu(11009L, "角色修改", "sys_role_edit", null, 11002L, null, 3, "1", now);
        insertMenu(11010L, "角色删除", "sys_role_del", null, 11002L, null, 4, "1", now);
        insertMenu(11011L, "角色授权", "sys_role_perm", null, 11002L, null, 5, "1", now);

        // 备件统计权限（仅配件管理员及以上）
        insertMenu(12000L, "备件查看", "spare_part:view", null, null, null, 0, "1", now);
        insertMenu(12001L, "备件新增/编辑", "spare_part:edit", null, null, null, 1, "1", now);
        insertMenu(12002L, "备件删除", "spare_part:delete", null, null, null, 2, "1", now);
        insertMenu(12003L, "种类管理", "spare_part:category_manage", null, null, null, 3, "1", now);

        // 角色授权：超级管理员全量；配件管理员 配件中心+备件管理；普通用户 查看/提交
        long[] adminMenus = {10000, 10001, 10002, 10003, 10004, 10005,
                11000, 11001, 11002, 11003, 11004, 11005, 11006, 11007, 11008, 11009, 11010, 11011,
                12000, 12001, 12002, 12003};
        for (long menuId : adminMenus) {
            insertRoleMenu(1L, menuId);
        }
        for (long menuId : new long[]{10000, 10001, 10003, 10004, 10005,
                12000, 12001, 12002, 12003}) {
            insertRoleMenu(3L, menuId);
        }
        for (long menuId : new long[]{10000, 10001, 10002}) {
            insertRoleMenu(2L, menuId);
        }
    }

    private void insertRole(Long id, String name, String code, String desc, LocalDateTime now) {
        entityManager.createNativeQuery(
                "INSERT INTO sys_role (role_id, role_name, role_code, role_desc, del_flag, create_time, update_time) " +
                        "VALUES (:id, :name, :code, :desc, '0', :now, :now) ON CONFLICT (role_id) DO NOTHING")
                .setParameter("id", id)
                .setParameter("name", name)
                .setParameter("code", code)
                .setParameter("desc", desc)
                .setParameter("now", now)
                .executeUpdate();
    }

    private void insertUser(Long id, String username, String displayName, String phone, String email, LocalDateTime now) {
        entityManager.createNativeQuery(
                "INSERT INTO sys_user (user_id, username, password, nickname, name, phone, email, lock_flag, del_flag, create_time, update_time) " +
                        "VALUES (:id, :username, :password, :name, :name, :phone, :email, '0', '0', :now, :now) " +
                        "ON CONFLICT (user_id) DO NOTHING")
                .setParameter("id", id)
                .setParameter("username", username)
                .setParameter("password", DEMO_PASSWORD_BCRYPT)
                .setParameter("name", displayName)
                .setParameter("phone", phone)
                .setParameter("email", email)
                .setParameter("now", now)
                .executeUpdate();
    }

    private void insertMenu(Long id, String name, String permission, String path,
                            Long parentId, String icon, int sortOrder, String menuType, LocalDateTime now) {
        entityManager.createNativeQuery(
                "INSERT INTO sys_menu (menu_id, name, permission, path, parent_id, icon, sort_order, menu_type, del_flag, create_time, update_time) " +
                        "VALUES (:id, :name, :permission, :path, :parentId, :icon, :sort, :type, '0', :now, :now) " +
                        "ON CONFLICT (menu_id) DO NOTHING")
                .setParameter("id", id)
                .setParameter("name", name)
                .setParameter("permission", permission)
                .setParameter("path", path)
                .setParameter("parentId", parentId)
                .setParameter("icon", icon)
                .setParameter("sort", sortOrder)
                .setParameter("type", menuType)
                .setParameter("now", now)
                .executeUpdate();
    }

    private void insertUserRole(Long userId, Long roleId) {
        entityManager.createNativeQuery(
                "INSERT INTO sys_user_role (user_id, role_id) VALUES (:userId, :roleId) " +
                        "ON CONFLICT (user_id, role_id) DO NOTHING")
                .setParameter("userId", userId)
                .setParameter("roleId", roleId)
                .executeUpdate();
    }

    private void insertRoleMenu(Long roleId, Long menuId) {
        entityManager.createNativeQuery(
                "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (:roleId, :menuId) " +
                        "ON CONFLICT (role_id, menu_id) DO NOTHING")
                .setParameter("roleId", roleId)
                .setParameter("menuId", menuId)
                .executeUpdate();
    }

    /**
     * 独立早事务推进自增序列：必须晚于种子事务提交，否则种子事务内已缓存的 nextval 会覆盖 setval 结果
     */
    @Component
    @Order(2)
    @RequiredArgsConstructor
    public static class SequenceSyncRunner implements ApplicationRunner {
        private final EntityManager entityManager;

        @Override
        @Transactional
        public void run(ApplicationArguments args) {
            syncSeq(entityManager, "sys_user", "sys_user_user_id_seq", "user_id");
            syncSeq(entityManager, "sys_role", "sys_role_role_id_seq", "role_id");
            syncSeq(entityManager, "sys_menu", "sys_menu_menu_id_seq", "menu_id");
        }

        /** 显式指定序列名：Hibernate 建表无 identity，pg_get_serial_sequence 会返回 NULL */
        private void syncSeq(EntityManager em, String table, String sequence, String column) {
            em.createNativeQuery(
                    "SELECT setval('" + sequence + "', " +
                            "COALESCE((SELECT MAX(" + column + ") FROM " + table + "), 0) + 1, false)")
                    .getSingleResult();
        }
    }
}

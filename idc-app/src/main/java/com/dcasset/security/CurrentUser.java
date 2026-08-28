package com.dcasset.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 当前登录用户（由 JWT 解析后装配，存入 SecurityContext）
 */
@Data
@Builder
public class CurrentUser {

    private Long id;
    private String username;
    private String nickname;
    private String name;
    /** 权限点（含 *:*:* 通配） */
    private Set<String> permissions;
    private List<RoleInfo> roles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfo {
        private Long roleId;
        private String roleName;
        private String roleCode;
    }
}

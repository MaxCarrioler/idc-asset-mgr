package com.dcasset.admin.dto;

import lombok.Data;

/**
 * 角色授权请求（/admin/role/menu）
 */
@Data
public class RoleMenuRequest {
    private Long roleId;
    /** 逗号分隔的菜单 ID，如 "10000,10001" */
    private String menuIds;
}

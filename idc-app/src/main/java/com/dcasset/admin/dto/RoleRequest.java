package com.dcasset.admin.dto;

import lombok.Data;

/**
 * 角色创建/编辑请求（/admin/role）
 */
@Data
public class RoleRequest {
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String roleDesc;
}

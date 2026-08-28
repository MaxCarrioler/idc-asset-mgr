package com.dcasset.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 编辑用户请求（/admin/user）
 */
@Data
public class UserUpdateRequest {
    private Long userId;
    private String username;
    /** 留空则不修改密码 */
    private String password;
    private String nickname;
    private String name;
    private String phone;
    private List<Long> role;
}

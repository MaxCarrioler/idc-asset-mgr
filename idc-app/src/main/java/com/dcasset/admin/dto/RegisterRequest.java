package com.dcasset.admin.dto;

import lombok.Data;

/**
 * 自助注册请求（/admin/register/user），密码为明文，注册后待审批
 */
@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String nickname;
    private String name;
    private String phone;
}

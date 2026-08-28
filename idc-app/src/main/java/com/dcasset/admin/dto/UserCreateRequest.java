package com.dcasset.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 新建用户请求（/admin/user）
 */
@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private String nickname;
    private String name;
    private String phone;
    /** '0' 正常；'9' 锁定 */
    private String lockFlag;
    /** 角色 ID 列表 */
    private List<Long> role;
}

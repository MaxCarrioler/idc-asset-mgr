package com.dcasset.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * /admin/user/info 响应体，字段与前端 stores/auth.js 约定一致
 */
@Data
public class UserInfoVO {
    private Long userId;
    private String username;
    private String name;
    private String nickname;
    private List<String> permissions;
    private List<RoleVO> roleList;
}

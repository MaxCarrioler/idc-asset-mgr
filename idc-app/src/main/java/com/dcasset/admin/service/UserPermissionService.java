package com.dcasset.admin.service;

import com.dcasset.admin.dto.RoleVO;
import com.dcasset.admin.dto.UserInfoVO;
import com.dcasset.admin.entity.SysMenuEntity;
import com.dcasset.admin.entity.SysRoleEntity;
import com.dcasset.admin.entity.SysRoleMenuEntity;
import com.dcasset.admin.entity.SysUserEntity;
import com.dcasset.admin.repository.SysMenuRepository;
import com.dcasset.admin.repository.SysRoleMenuRepository;
import com.dcasset.admin.repository.SysRoleRepository;
import com.dcasset.admin.repository.SysUserRepository;
import com.dcasset.admin.repository.SysUserRoleRepository;
import com.dcasset.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户角色 / 权限点聚合：登录装配与 /admin/user/info 共用
 */
@Service
@RequiredArgsConstructor
public class UserPermissionService {

    public static final String ADMIN_ROLE_CODE = "ROLE_ADMIN";
    public static final String ALL_PERMISSION = "*:*:*";

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleRepository roleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysMenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    /** 校验明文密码 */
    public boolean verifyPassword(String username, String rawPassword) {
        return userRepository.findByUsernameAndDelFlag(username, "0")
                .map(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .orElse(false);
    }

    /**
     * 装配可登录用户：存在、未删除、已审批通过；否则返回 null
     */
    public CurrentUser loadActiveUser(String username) {
        SysUserEntity user = userRepository.findByUsernameAndDelFlag(username, "0").orElse(null);
        if (user == null || !"0".equals(user.getLockFlag())) {
            return null;
        }
        List<SysRoleEntity> roles = loadRoles(user.getUserId());
        Set<String> permissions = loadPermissions(roles);
        return CurrentUser.builder()
                .id(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .name(user.getName())
                .permissions(permissions)
                .roles(roles.stream().map(this::toRoleInfo).collect(Collectors.toList()))
                .build();
    }

    /**
     * /admin/user/info 响应
     */
    public UserInfoVO loadUserInfo(String username) {
        CurrentUser user = loadActiveUser(username);
        if (user == null) {
            return null;
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setNickname(user.getNickname());
        vo.setPermissions(new ArrayList<>(user.getPermissions()));
        vo.setRoleList(user.getRoles().stream()
                .map(r -> RoleVO.builder()
                        .roleId(r.getRoleId())
                        .roleName(r.getRoleName())
                        .roleCode(r.getRoleCode())
                        .build())
                .collect(Collectors.toList()));
        return vo;
    }

    public List<SysRoleEntity> loadRoles(Long userId) {
        List<Long> roleIds = userRoleRepository.findByUserId(userId).stream()
                .map(ur -> ur.getRoleId())
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return List.of();
        }
        Map<Long, SysRoleEntity> roleMap = roleRepository.findAllById(roleIds).stream()
                .filter(r -> "0".equals(r.getDelFlag()))
                .collect(Collectors.toMap(SysRoleEntity::getRoleId, Function.identity()));
        return roleIds.stream()
                .map(roleMap::get)
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    private Set<String> loadPermissions(List<SysRoleEntity> roles) {
        Set<String> permissions = new LinkedHashSet<>();
        if (roles.isEmpty()) {
            return permissions;
        }
        boolean isAdmin = roles.stream().anyMatch(r -> ADMIN_ROLE_CODE.equals(r.getRoleCode()));
        List<Long> roleIds = roles.stream().map(SysRoleEntity::getRoleId).collect(Collectors.toList());
        Set<Long> menuIds = roleMenuRepository.findByRoleIdIn(roleIds).stream()
                .map(SysRoleMenuEntity::getMenuId)
                .collect(Collectors.toSet());
        if (!menuIds.isEmpty()) {
            menuRepository.findAllById(menuIds).stream()
                    .filter(m -> "0".equals(m.getDelFlag()))
                    .map(SysMenuEntity::getPermission)
                    .filter(p -> p != null && !p.isBlank())
                    .forEach(permissions::add);
        }
        if (isAdmin) {
            // 超管拥有全部权限点与通配符
            permissions.add(ALL_PERMISSION);
            menuRepository.findByDelFlagOrderBySortOrderAsc("0").stream()
                    .map(SysMenuEntity::getPermission)
                    .filter(p -> p != null && !p.isBlank())
                    .forEach(permissions::add);
        }
        return permissions;
    }

    public CurrentUser.RoleInfo toRoleInfo(SysRoleEntity role) {
        return CurrentUser.RoleInfo.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .build();
    }

    public RoleVO toRoleVo(SysRoleEntity role) {
        return RoleVO.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .roleDesc(role.getRoleDesc())
                .build();
    }
}

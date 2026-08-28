package com.dcasset.admin.service;

import com.dcasset.admin.dto.RoleMenuRequest;
import com.dcasset.admin.dto.RoleRequest;
import com.dcasset.admin.dto.RoleVO;
import com.dcasset.admin.entity.SysRoleEntity;
import com.dcasset.admin.entity.SysRoleMenuEntity;
import com.dcasset.admin.repository.SysMenuRepository;
import com.dcasset.admin.repository.SysRoleMenuRepository;
import com.dcasset.admin.repository.SysRoleRepository;
import com.dcasset.admin.repository.SysUserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户中心：角色管理与角色授权
 */
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final SysRoleRepository roleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysMenuRepository menuRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final UserPermissionService permissionService;

    public List<RoleVO> list() {
        return roleRepository.findByDelFlagOrderByRoleIdAsc("0").stream()
                .map(permissionService::toRoleVo)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleVO create(RoleRequest request) {
        if (request.getRoleName() == null || request.getRoleName().isBlank()) {
            throw new IllegalArgumentException("角色名不能为空");
        }
        if (request.getRoleCode() == null || request.getRoleCode().isBlank()) {
            throw new IllegalArgumentException("角色码不能为空");
        }
        String roleCode = request.getRoleCode().trim();
        if (UserPermissionService.ADMIN_ROLE_CODE.equals(roleCode)) {
            throw new IllegalArgumentException("不可创建超级管理员角色");
        }
        if (roleRepository.findByRoleCodeAndDelFlag(roleCode, "0").isPresent()) {
            throw new IllegalArgumentException("角色码已存在: " + roleCode);
        }
        SysRoleEntity entity = new SysRoleEntity();
        entity.setRoleName(request.getRoleName().trim());
        entity.setRoleCode(roleCode);
        entity.setRoleDesc(request.getRoleDesc());
        return permissionService.toRoleVo(roleRepository.save(entity));
    }

    @Transactional
    public RoleVO update(RoleRequest request) {
        if (request.getRoleId() == null) {
            throw new IllegalArgumentException("缺少角色 ID");
        }
        SysRoleEntity entity = findActive(request.getRoleId());
        if (UserPermissionService.ADMIN_ROLE_CODE.equals(entity.getRoleCode())) {
            throw new IllegalArgumentException("超级管理员角色不可修改");
        }
        if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
            entity.setRoleName(request.getRoleName().trim());
        }
        if (request.getRoleDesc() != null) {
            entity.setRoleDesc(request.getRoleDesc());
        }
        return permissionService.toRoleVo(roleRepository.save(entity));
    }

    @Transactional
    public void delete(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            SysRoleEntity entity = findActive(roleId);
            if (UserPermissionService.ADMIN_ROLE_CODE.equals(entity.getRoleCode())) {
                throw new IllegalArgumentException("超级管理员角色不可删除");
            }
            if (!userRoleRepository.findByRoleId(roleId).isEmpty()) {
                throw new IllegalArgumentException("角色「" + entity.getRoleName() + "」仍有用户关联，不可删除");
            }
            entity.setDelFlag("1");
            roleRepository.save(entity);
            roleMenuRepository.deleteByRoleId(roleId);
        }
    }

    @Transactional
    public void saveRoleMenus(RoleMenuRequest request) {
        if (request == null || request.getRoleId() == null) {
            throw new IllegalArgumentException("缺少角色 ID");
        }
        SysRoleEntity entity = findActive(request.getRoleId());
        if (UserPermissionService.ADMIN_ROLE_CODE.equals(entity.getRoleCode())) {
            throw new IllegalArgumentException("超级管理员角色授权固定，不可修改");
        }
        Set<Long> validMenuIds = menuRepository.findByDelFlagOrderBySortOrderAsc("0").stream()
                .map(m -> m.getMenuId())
                .collect(Collectors.toSet());
        roleMenuRepository.deleteByRoleId(request.getRoleId());
        if (request.getMenuIds() == null || request.getMenuIds().isBlank()) {
            return;
        }
        for (String part : request.getMenuIds().split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Long menuId;
            try {
                menuId = Long.valueOf(trimmed);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("菜单 ID 非法: " + trimmed);
            }
            if (!validMenuIds.contains(menuId)) {
                throw new IllegalArgumentException("菜单不存在: " + menuId);
            }
            SysRoleMenuEntity rel = new SysRoleMenuEntity();
            rel.setRoleId(request.getRoleId());
            rel.setMenuId(menuId);
            roleMenuRepository.save(rel);
        }
    }

    private SysRoleEntity findActive(Long roleId) {
        SysRoleEntity entity = roleRepository.findById(roleId).orElse(null);
        if (entity == null || "1".equals(entity.getDelFlag())) {
            throw new IllegalArgumentException("角色不存在: " + roleId);
        }
        return entity;
    }
}

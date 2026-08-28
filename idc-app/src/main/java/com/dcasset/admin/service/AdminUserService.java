package com.dcasset.admin.service;

import com.dcasset.admin.dto.*;
import com.dcasset.admin.entity.SysRoleEntity;
import com.dcasset.admin.entity.SysUserEntity;
import com.dcasset.admin.entity.SysUserRoleEntity;
import com.dcasset.admin.repository.SysRoleRepository;
import com.dcasset.admin.repository.SysUserRepository;
import com.dcasset.admin.repository.SysUserRoleRepository;
import com.dcasset.common.result.PageResult;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户中心：用户分页 / 建号 / 编辑 / 注销 / 审批 / 驳回 / 自助注册
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleRepository roleRepository;
    private final UserPermissionService permissionService;
    private final PasswordEncoder passwordEncoder;

    /** 通用角色：注册/建号未指定角色时默认 */
    private static final String GENERAL_ROLE_CODE = "GENERAL_USER";

    public PageResult<UserVO> page(int current, int size, String username, String lockFlag) {
        Specification<SysUserEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("delFlag"), "0"));
            if (username != null && !username.isBlank()) {
                predicates.add(cb.like(root.get("username"), "%" + username.trim() + "%"));
            }
            if (lockFlag != null && !lockFlag.isBlank()) {
                predicates.add(cb.equal(root.get("lockFlag"), lockFlag.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SysUserEntity> page = userRepository.findAll(spec,
                PageRequest.of(Math.max(current - 1, 0), size, Sort.by(Sort.Direction.ASC, "userId")));
        List<UserVO> records = page.getContent().stream().map(this::toVo).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotalElements(), current, size, page.getTotalPages());
    }

    public UserVO details(Long userId) {
        return toVo(findActive(userId));
    }

    @Transactional
    public UserVO create(UserCreateRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        String username = request.getUsername().trim();
        if (userRepository.existsByUsernameAndDelFlag(username, "0")) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        SysUserEntity entity = new SysUserEntity();
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setNickname(blankToNull(request.getNickname()));
        entity.setName(blankToNull(request.getName()));
        entity.setPhone(blankToNull(request.getPhone()));
        entity.setLockFlag("9".equals(request.getLockFlag()) ? "9" : "0");
        entity = userRepository.save(entity);

        List<Long> roleIds = request.getRole() == null ? List.of() : request.getRole();
        Long adminRoleId = findAdminRoleId();
        if (adminRoleId != null && roleIds.contains(adminRoleId)) {
            throw new IllegalArgumentException("不可通过建号授予超级管理员角色");
        }
        assignRoles(entity.getUserId(), roleIds);
        return toVo(entity);
    }

    @Transactional
    public UserVO update(UserUpdateRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("缺少用户 ID");
        }
        SysUserEntity entity = findActive(request.getUserId());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getNickname() != null) {
            entity.setNickname(blankToNull(request.getNickname()));
        }
        if (request.getName() != null) {
            entity.setName(blankToNull(request.getName()));
        }
        if (request.getPhone() != null) {
            entity.setPhone(blankToNull(request.getPhone()));
        }
        userRepository.save(entity);

        if (request.getRole() != null) {
            replaceRoles(entity, request.getRole());
        }
        return toVo(entity);
    }

    @Transactional
    public void delete(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            SysUserEntity entity = findActive(userId);
            assertNotLastAdmin(entity);
            entity.setDelFlag("1");
            userRepository.save(entity);
            userRoleRepository.deleteByUserId(userId);
        }
    }

    @Transactional
    public void approve(Long userId) {
        SysUserEntity entity = findActive(userId);
        if (!"9".equals(entity.getLockFlag())) {
            throw new IllegalArgumentException("该用户不处于待审批状态");
        }
        entity.setLockFlag("0");
        if (userRoleRepository.findByUserId(userId).isEmpty()) {
            assignRoles(userId, List.of());
        }
        userRepository.save(entity);
    }

    /** 驳回注册：软删除 */
    @Transactional
    public void reject(Long userId) {
        SysUserEntity entity = findActive(userId);
        assertNotLastAdmin(entity);
        entity.setDelFlag("1");
        userRepository.save(entity);
        userRoleRepository.deleteByUserId(userId);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        String username = request.getUsername().trim();
        if (userRepository.existsByUsernameAndDelFlag(username, "0")) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        SysUserEntity entity = new SysUserEntity();
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));
        entity.setNickname(blankToNull(request.getNickname()));
        entity.setName(blankToNull(request.getName()));
        entity.setPhone(blankToNull(request.getPhone()));
        entity.setLockFlag("9");
        entity = userRepository.save(entity);
        assignRoles(entity.getUserId(), List.of());
    }

    private SysUserEntity findActive(Long userId) {
        SysUserEntity entity = userRepository.findById(userId).orElse(null);
        if (entity == null || "1".equals(entity.getDelFlag())) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }
        return entity;
    }

    /** 业务侧不可创建/删除/篡改超管角色关联（保留原有超管角色除外） */
    private void replaceRoles(SysUserEntity entity, List<Long> roleIds) {
        Long adminRoleId = findAdminRoleId();
        Set<Long> before = userRoleRepository.findByUserId(entity.getUserId()).stream()
                .map(SysUserRoleEntity::getRoleId)
                .collect(Collectors.toSet());
        Set<Long> after = new HashSet<>();
        for (Long roleId : roleIds) {
            if (roleId != null) {
                after.add(roleId);
            }
        }
        if (adminRoleId != null) {
            boolean hadAdmin = before.contains(adminRoleId);
            if (!hadAdmin && after.contains(adminRoleId)) {
                throw new IllegalArgumentException("不可通过编辑授予超级管理员角色");
            }
            if (hadAdmin && !after.contains(adminRoleId) && countAdmins() <= 1) {
                throw new IllegalArgumentException("不可移除唯一超级管理员的管理员角色");
            }
        }
        userRoleRepository.deleteByUserId(entity.getUserId());
        assignRoles(entity.getUserId(), new ArrayList<>(after));
    }

    private void assignRoles(Long userId, List<Long> roleIds) {
        Set<Long> assigned = new HashSet<>();
        if (roleIds == null || roleIds.isEmpty()) {
            roleRepository.findByRoleCodeAndDelFlag(GENERAL_ROLE_CODE, "0").ifPresent(role -> {
                SysUserRoleEntity rel = new SysUserRoleEntity();
                rel.setUserId(userId);
                rel.setRoleId(role.getRoleId());
                userRoleRepository.save(rel);
            });
            return;
        }
        for (Long roleId : roleIds) {
            if (roleId != null && assigned.add(roleId) && roleRepository.existsById(roleId)) {
                SysUserRoleEntity rel = new SysUserRoleEntity();
                rel.setUserId(userId);
                rel.setRoleId(roleId);
                userRoleRepository.save(rel);
            }
        }
    }

    private void assertNotLastAdmin(SysUserEntity entity) {
        Long adminRoleId = findAdminRoleId();
        if (adminRoleId == null) {
            return;
        }
        boolean isAdmin = userRoleRepository.findByUserId(entity.getUserId()).stream()
                .anyMatch(rel -> adminRoleId.equals(rel.getRoleId()));
        if (isAdmin && countAdmins() <= 1) {
            throw new IllegalArgumentException("不可删除最后一个超级管理员");
        }
    }

    private long countAdmins() {
        Long adminRoleId = findAdminRoleId();
        if (adminRoleId == null) {
            return 0;
        }
        Set<Long> adminUserIds = userRoleRepository.findByRoleId(adminRoleId).stream()
                .map(SysUserRoleEntity::getUserId)
                .collect(Collectors.toSet());
        return userRepository.findAllById(adminUserIds).stream()
                .filter(u -> "0".equals(u.getDelFlag()))
                .count();
    }

    private Long findAdminRoleId() {
        return roleRepository.findByRoleCodeAndDelFlag(UserPermissionService.ADMIN_ROLE_CODE, "0")
                .map(SysRoleEntity::getRoleId)
                .orElse(null);
    }

    private UserVO toVo(SysUserEntity entity) {
        UserVO vo = new UserVO();
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setName(entity.getName());
        vo.setPhone(entity.getPhone());
        vo.setLockFlag(entity.getLockFlag());
        vo.setCreateTime(entity.getCreateTime());
        vo.setRoleList(permissionService.loadRoles(entity.getUserId()).stream()
                .map(permissionService::toRoleVo)
                .collect(Collectors.toList()));
        return vo;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

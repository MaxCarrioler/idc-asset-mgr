package com.dcasset.admin.controller;

import com.dcasset.admin.dto.*;
import com.dcasset.admin.service.AdminUserService;
import com.dcasset.admin.service.UserPermissionService;
import com.dcasset.common.result.PageResult;
import com.dcasset.common.result.Result;
import com.dcasset.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户中心")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService userService;
    private final UserPermissionService permissionService;

    @Operation(summary = "当前登录用户信息")
    @GetMapping("/user/info")
    public Result<UserInfoVO> userInfo() {
        String username = SecurityUtils.getUsername();
        UserInfoVO vo = username == null ? null : permissionService.loadUserInfo(username);
        if (vo == null) {
            return Result.fail(401, "用户信息不可用");
        }
        return Result.success(vo);
    }

    @Operation(summary = "用户分页")
    @PreAuthorize("hasAuthority('sys_user_view')")
    @GetMapping("/user/page")
    public Result<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String lockFlag) {
        return Result.success(userService.page(current, size, username, lockFlag));
    }

    @Operation(summary = "用户详情")
    @PreAuthorize("hasAuthority('sys_user_view')")
    @GetMapping("/user/details/{id}")
    public Result<UserVO> details(@PathVariable Long id) {
        return Result.success(userService.details(id));
    }

    @Operation(summary = "新建用户")
    @PreAuthorize("hasAuthority('sys_user_add')")
    @PostMapping("/user")
    public Result<UserVO> create(@RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @Operation(summary = "编辑用户 / 分配角色")
    @PreAuthorize("hasAuthority('sys_user_edit')")
    @PutMapping("/user")
    public Result<UserVO> update(@RequestBody UserUpdateRequest request) {
        return Result.success(userService.update(request));
    }

    @Operation(summary = "注销用户")
    @PreAuthorize("hasAuthority('sys_user_del')")
    @DeleteMapping("/user")
    public Result<Void> delete(@RequestBody List<Long> ids) {
        userService.delete(ids);
        return Result.success(null);
    }

    @Operation(summary = "审批通过")
    @PreAuthorize("hasAuthority('sys_user_edit')")
    @PutMapping("/user/approve/{id}")
    public Result<Void> approve(@PathVariable Long id) {
        userService.approve(id);
        return Result.success(null);
    }

    @Operation(summary = "驳回注册（软删除）")
    @PreAuthorize("hasAuthority('sys_user_del')")
    @DeleteMapping("/user/reject/{id}")
    public Result<Void> reject(@PathVariable Long id) {
        userService.reject(id);
        return Result.success(null);
    }

    @Operation(summary = "自助注册（待审批）")
    @PostMapping("/register/user")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success(null);
    }
}

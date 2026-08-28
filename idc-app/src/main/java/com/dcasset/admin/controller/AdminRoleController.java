package com.dcasset.admin.controller;

import com.dcasset.admin.dto.RoleMenuRequest;
import com.dcasset.admin.dto.RoleRequest;
import com.dcasset.admin.dto.RoleVO;
import com.dcasset.admin.service.AdminRoleService;
import com.dcasset.common.result.PageResult;
import com.dcasset.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户中心-角色")
@RestController
@RequestMapping("/admin/role")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleService roleService;

    @Operation(summary = "角色列表")
    @PreAuthorize("hasAnyAuthority('sys_role_view','sys_user_view','sys_user_add','sys_user_edit')")
    @GetMapping("/list")
    public Result<List<RoleVO>> list() {
        return Result.success(roleService.list());
    }

    @Operation(summary = "角色分页")
    @PreAuthorize("hasAuthority('sys_role_view')")
    @GetMapping("/page")
    public Result<PageResult<RoleVO>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        List<RoleVO> all = roleService.list();
        int from = Math.min((current - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        return Result.success(new PageResult<>(all.subList(from, to), all.size(), current, size,
                (all.size() + size - 1) / size));
    }

    @Operation(summary = "新建角色")
    @PreAuthorize("hasAuthority('sys_role_add')")
    @PostMapping
    public Result<RoleVO> create(@RequestBody RoleRequest request) {
        return Result.success(roleService.create(request));
    }

    @Operation(summary = "编辑角色")
    @PreAuthorize("hasAuthority('sys_role_edit')")
    @PutMapping
    public Result<RoleVO> update(@RequestBody RoleRequest request) {
        return Result.success(roleService.update(request));
    }

    @Operation(summary = "删除角色")
    @PreAuthorize("hasAuthority('sys_role_del')")
    @DeleteMapping
    public Result<Void> delete(@RequestBody List<Long> ids) {
        roleService.delete(ids);
        return Result.success(null);
    }

    @Operation(summary = "保存角色菜单授权")
    @PreAuthorize("hasAuthority('sys_role_perm')")
    @PutMapping("/menu")
    public Result<Void> saveRoleMenus(@RequestBody RoleMenuRequest request) {
        roleService.saveRoleMenus(request);
        return Result.success(null);
    }
}

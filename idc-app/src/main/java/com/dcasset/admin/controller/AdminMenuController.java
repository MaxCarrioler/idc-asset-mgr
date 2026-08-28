package com.dcasset.admin.controller;

import com.dcasset.admin.service.AdminMenuService;
import com.dcasset.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户中心-菜单")
@RestController
@RequestMapping("/admin/menu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final AdminMenuService menuService;

    @Operation(summary = "菜单树")
    @PreAuthorize("hasAuthority('sys_role_perm')")
    @GetMapping("/tree")
    public Result<List<AdminMenuService.MenuNodeVO>> tree() {
        return Result.success(menuService.tree());
    }

    @Operation(summary = "角色已授权菜单 ID")
    @PreAuthorize("hasAuthority('sys_role_perm')")
    @GetMapping("/tree/{roleId}")
    public Result<List<Long>> roleMenuIds(@PathVariable Long roleId) {
        return Result.success(menuService.roleMenuIds(roleId));
    }
}

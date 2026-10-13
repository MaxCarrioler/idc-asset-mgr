package com.dcasset.accessory.controller;

import com.dcasset.accessory.service.SparePartCategoryService;
import com.dcasset.common.dto.SparePartCategoryDTO;
import com.dcasset.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "备件种类")
@RestController
@RequestMapping("/api/spare-part-categories")
@RequiredArgsConstructor
public class SparePartCategoryController {

    private final SparePartCategoryService categoryService;

    @Operation(summary = "备件种类列表")
    @GetMapping
    @PreAuthorize("hasAuthority('spare_part:view')")
    public Result<List<SparePartCategoryDTO>> list() {
        return Result.success(categoryService.list());
    }

    @Operation(summary = "备件种类详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('spare_part:view')")
    public Result<SparePartCategoryDTO> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @Operation(summary = "新增备件种类")
    @PostMapping
    @PreAuthorize("hasAuthority('spare_part:category_manage')")
    public Result<SparePartCategoryDTO> create(@RequestBody SparePartCategoryDTO dto) {
        return Result.success(categoryService.create(dto));
    }

    @Operation(summary = "更新备件种类")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('spare_part:category_manage')")
    public Result<SparePartCategoryDTO> update(@PathVariable Long id, @RequestBody SparePartCategoryDTO dto) {
        return Result.success(categoryService.update(id, dto));
    }

    @Operation(summary = "删除备件种类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('spare_part:category_manage')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success("删除成功", null);
    }
}

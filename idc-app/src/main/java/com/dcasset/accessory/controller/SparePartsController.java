package com.dcasset.accessory.controller;

import com.dcasset.accessory.service.SparePartsService;
import com.dcasset.common.dto.SparePartDTO;
import com.dcasset.common.dto.SparePartSummaryDTO;
import com.dcasset.common.dto.SparePartTypeSummaryDTO;
import com.dcasset.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "备件库存")
@RestController
@RequestMapping("/api/spare-parts")
@RequiredArgsConstructor
public class SparePartsController {

    private final SparePartsService sparePartsService;

    @Operation(summary = "备件库存汇总（按种类统计）")
    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('spare_part:view')")
    public Result<List<SparePartSummaryDTO>> getSummary() {
        return Result.success(sparePartsService.getSummary());
    }

    @Operation(summary = "按配件大类汇总统计")
    @GetMapping("/type-summaries")
    @PreAuthorize("hasAuthority('spare_part:view')")
    public Result<List<SparePartTypeSummaryDTO>> getTypeSummaries() {
        return Result.success(sparePartsService.getTypeSummaries());
    }

    @Operation(summary = "按配件大类查询种类列表")
    @GetMapping("/by-type/{type}")
    @PreAuthorize("hasAuthority('spare_part:view')")
    public Result<List<SparePartSummaryDTO>> listByType(@PathVariable String type) {
        return Result.success(sparePartsService.listByType(type));
    }

    @Operation(summary = "按配件大类查询全部备件明细")
    @GetMapping("/items/by-type/{type}")
    @PreAuthorize("hasAuthority('spare_part:view')")
    public Result<List<SparePartDTO>> listItemsByType(@PathVariable String type) {
        return Result.success(sparePartsService.listItemsByType(type));
    }

    @Operation(summary = "按种类查询备件明细")
    @GetMapping("/items")
    @PreAuthorize("hasAuthority('spare_part:view')")
    public Result<List<SparePartDTO>> listByCategory(@RequestParam Long categoryId) {
        return Result.success(sparePartsService.listByCategory(categoryId));
    }

    @Operation(summary = "备件详情")
    @GetMapping("/items/{id}")
    @PreAuthorize("hasAuthority('spare_part:view')")
    public Result<SparePartDTO> getById(@PathVariable Long id) {
        return Result.success(sparePartsService.getById(id));
    }

    @Operation(summary = "新增备件")
    @PostMapping("/items")
    @PreAuthorize("hasAuthority('spare_part:edit')")
    public Result<SparePartDTO> create(@RequestBody SparePartDTO dto) {
        return Result.success(sparePartsService.create(dto));
    }

    @Operation(summary = "更新备件")
    @PutMapping("/items/{id}")
    @PreAuthorize("hasAuthority('spare_part:edit')")
    public Result<SparePartDTO> update(@PathVariable Long id, @RequestBody SparePartDTO dto) {
        return Result.success(sparePartsService.update(id, dto));
    }

    @Operation(summary = "删除备件")
    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasAuthority('spare_part:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        sparePartsService.delete(id);
        return Result.success("删除成功", null);
    }
}

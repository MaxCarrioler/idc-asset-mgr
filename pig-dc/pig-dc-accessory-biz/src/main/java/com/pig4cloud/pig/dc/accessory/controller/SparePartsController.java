package com.pig4cloud.pig.dc.accessory.controller;

import com.pig4cloud.pig.dc.accessory.service.SparePartsService;
import com.pig4cloud.pig.dc.common.dto.SparePartDTO;
import com.pig4cloud.pig.dc.common.dto.SparePartSummaryDTO;
import com.pig4cloud.pig.dc.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public Result<List<SparePartSummaryDTO>> getSummary() {
        return Result.success(sparePartsService.getSummary());
    }

    @Operation(summary = "按种类查询备件明细")
    @GetMapping("/items")
    public Result<List<SparePartDTO>> listByCategory(@RequestParam Long categoryId) {
        return Result.success(sparePartsService.listByCategory(categoryId));
    }

    @Operation(summary = "备件详情")
    @GetMapping("/items/{id}")
    public Result<SparePartDTO> getById(@PathVariable Long id) {
        return Result.success(sparePartsService.getById(id));
    }

    @Operation(summary = "新增备件")
    @PostMapping("/items")
    public Result<SparePartDTO> create(@RequestBody SparePartDTO dto) {
        return Result.success(sparePartsService.create(dto));
    }

    @Operation(summary = "更新备件")
    @PutMapping("/items/{id}")
    public Result<SparePartDTO> update(@PathVariable Long id, @RequestBody SparePartDTO dto) {
        return Result.success(sparePartsService.update(id, dto));
    }

    @Operation(summary = "删除备件")
    @DeleteMapping("/items/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sparePartsService.delete(id);
        return Result.success("删除成功", null);
    }
}

package com.dcasset.server.controller;

import com.dcasset.common.dto.HardDiskTypeDTO;
import com.dcasset.common.result.Result;
import com.dcasset.server.service.HardDiskTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "硬盘类型元数据")
@RestController
@RequestMapping("/api/hard-disk-types")
@RequiredArgsConstructor
public class HardDiskTypeController {

    private final HardDiskTypeService hardDiskTypeService;

    @Operation(summary = "获取全部硬盘类型")
    @GetMapping
    public Result<List<HardDiskTypeDTO>> list() {
        return Result.success(hardDiskTypeService.list());
    }

    @Operation(summary = "获取硬盘类型详情")
    @GetMapping("/{id}")
    public Result<HardDiskTypeDTO> getById(@PathVariable Long id) {
        return Result.success(hardDiskTypeService.getById(id));
    }

    @Operation(summary = "新增硬盘类型")
    @PostMapping
    public Result<HardDiskTypeDTO> create(@Valid @RequestBody HardDiskTypeDTO dto) {
        return Result.success("创建成功", hardDiskTypeService.create(dto));
    }

    @Operation(summary = "更新硬盘类型")
    @PutMapping("/{id}")
    public Result<HardDiskTypeDTO> update(@PathVariable Long id, @Valid @RequestBody HardDiskTypeDTO dto) {
        return Result.success("更新成功", hardDiskTypeService.update(id, dto));
    }

    @Operation(summary = "删除硬盘类型")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        hardDiskTypeService.delete(id);
        return Result.success("删除成功", null);
    }
}

package com.pig4cloud.pig.dc.server.controller;

import com.pig4cloud.pig.dc.common.dto.CpuArchitectureDTO;
import com.pig4cloud.pig.dc.common.result.Result;
import com.pig4cloud.pig.dc.server.service.CpuArchitectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CPU 架构元数据")
@RestController
@RequestMapping("/api/cpu-architectures")
@RequiredArgsConstructor
public class CpuArchitectureController {

    private final CpuArchitectureService cpuArchitectureService;

    @Operation(summary = "获取全部 CPU 架构")
    @GetMapping
    public Result<List<CpuArchitectureDTO>> list() {
        return Result.success(cpuArchitectureService.list());
    }

    @Operation(summary = "获取 CPU 架构详情")
    @GetMapping("/{id}")
    public Result<CpuArchitectureDTO> getById(@PathVariable Long id) {
        return Result.success(cpuArchitectureService.getById(id));
    }

    @Operation(summary = "新增 CPU 架构")
    @PostMapping
    public Result<CpuArchitectureDTO> create(@Valid @RequestBody CpuArchitectureDTO dto) {
        return Result.success("创建成功", cpuArchitectureService.create(dto));
    }

    @Operation(summary = "更新 CPU 架构")
    @PutMapping("/{id}")
    public Result<CpuArchitectureDTO> update(@PathVariable Long id, @Valid @RequestBody CpuArchitectureDTO dto) {
        return Result.success("更新成功", cpuArchitectureService.update(id, dto));
    }

    @Operation(summary = "删除 CPU 架构")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cpuArchitectureService.delete(id);
        return Result.success("删除成功", null);
    }
}

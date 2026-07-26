package com.pig4cloud.pig.dc.server.controller;

import com.pig4cloud.pig.dc.common.dto.ServerDTO;
import com.pig4cloud.pig.dc.common.dto.ServerOperationRecordDTO;
import com.pig4cloud.pig.dc.common.result.PageResult;
import com.pig4cloud.pig.dc.common.result.Result;
import com.pig4cloud.pig.dc.server.service.ServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "服务器管理", description = "服务器基础信息 CRUD")
@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    @Operation(summary = "分页查询服务器列表")
    @GetMapping
    public Result<PageResult<ServerDTO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return Result.success(serverService.list(keyword, page, size));
    }

    @Operation(summary = "获取服务器详情")
    @GetMapping("/{id}")
    public Result<ServerDTO> getById(@PathVariable Long id) {
        return Result.success(serverService.getById(id));
    }

    @Operation(summary = "新增服务器")
    @PostMapping
    public Result<ServerDTO> create(@Valid @RequestBody ServerDTO dto) {
        return Result.success("创建成功", serverService.create(dto));
    }

    @Operation(summary = "更新服务器")
    @PutMapping("/{id}")
    public Result<ServerDTO> update(@PathVariable Long id, @Valid @RequestBody ServerDTO dto) {
        return Result.success("更新成功", serverService.update(id, dto));
    }

    @Operation(summary = "删除服务器")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serverService.delete(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "获取服务器操作记录")
    @GetMapping("/{id}/operation-records")
    public Result<List<ServerOperationRecordDTO>> listOperationRecords(@PathVariable Long id) {
        return Result.success(serverService.listOperationRecords(id));
    }

    @Operation(summary = "获取全量 Region 区域名称（用于统计筛选）")
    @GetMapping("/regions")
    public Result<List<String>> listRegions() {
        return Result.success(serverService.listRegions());
    }

    @Operation(summary = "获取全部 CPU 架构选项（用于统计筛选）")
    @GetMapping("/cpu-architectures")
    public Result<List<String>> listCpuArchitectures() {
        return Result.success(serverService.listCpuArchitectures());
    }
}

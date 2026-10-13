package com.dcasset.server.controller;

import com.dcasset.common.dto.ServerTypeDTO;
import com.dcasset.common.result.Result;
import com.dcasset.server.service.ServerTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "服务器类型管理")
@RestController
@RequestMapping("/api/server-types")
@RequiredArgsConstructor
public class ServerTypeController {

    private final ServerTypeService serverTypeService;

    @Operation(summary = "获取全部服务器类型")
    @GetMapping
    public Result<List<ServerTypeDTO>> list() {
        return Result.success(serverTypeService.list());
    }

    @Operation(summary = "获取服务器类型详情")
    @GetMapping("/{id}")
    public Result<ServerTypeDTO> getById(@PathVariable Long id) {
        return Result.success(serverTypeService.getById(id));
    }

    @Operation(summary = "新增服务器类型")
    @PostMapping
    public Result<ServerTypeDTO> create(@Valid @RequestBody ServerTypeDTO dto) {
        return Result.success("创建成功", serverTypeService.create(dto));
    }

    @Operation(summary = "更新服务器类型")
    @PutMapping("/{id}")
    public Result<ServerTypeDTO> update(@PathVariable Long id, @Valid @RequestBody ServerTypeDTO dto) {
        return Result.success("更新成功", serverTypeService.update(id, dto));
    }

    @Operation(summary = "删除服务器类型")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serverTypeService.delete(id);
        return Result.success("删除成功", null);
    }
}

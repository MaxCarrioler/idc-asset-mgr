package com.dcasset.accessory.controller;

import com.dcasset.common.result.Result;
import com.dcasset.accessory.service.BaseAccessoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseAccessoryController<D> {

    protected abstract BaseAccessoryService<?, D> getService();

    protected abstract String getTypeName();

    @GetMapping
    public Result<List<D>> listByServer(@RequestParam("serverId") Long serverId) {
        return Result.success(getService().listByServerId(serverId));
    }

    @GetMapping("/{id}")
    public Result<D> getById(@PathVariable Long id) {
        return Result.success(getService().getById(id));
    }

    @PostMapping
    public Result<D> create(@Valid @RequestBody D dto) {
        return Result.success("新增" + getTypeName() + "成功", getService().create(dto));
    }

    @PutMapping("/{id}")
    public Result<D> update(@PathVariable Long id, @Valid @RequestBody D dto) {
        return Result.success("更新" + getTypeName() + "成功", getService().update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        getService().delete(id);
        return Result.success("删除" + getTypeName() + "成功", null);
    }
}

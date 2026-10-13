package com.dcasset.accessory.controller;

import com.dcasset.common.dto.ServerAccessorySummaryDTO;
import com.dcasset.common.result.Result;
import com.dcasset.accessory.service.AccessorySummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "服务器配件汇总")
@RestController
@RequestMapping("/api/server-accessories")
@RequiredArgsConstructor
public class AccessorySummaryController {

    private final AccessorySummaryService accessorySummaryService;

    @Operation(summary = "获取服务器全部配件汇总")
    @GetMapping("/{serverId}")
    public Result<ServerAccessorySummaryDTO> getSummary(@PathVariable Long serverId) {
        return Result.success(accessorySummaryService.getSummaryByServerId(serverId));
    }
}

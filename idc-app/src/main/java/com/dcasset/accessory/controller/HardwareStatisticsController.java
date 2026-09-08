package com.dcasset.accessory.controller;

import com.dcasset.accessory.service.HardwareStatisticsService;
import com.dcasset.common.dto.HardwareStatisticsDTO;
import com.dcasset.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "硬件资源统计")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class HardwareStatisticsController {

    private final HardwareStatisticsService hardwareStatisticsService;

    @Operation(summary = "硬件资源汇总统计（支持 Region / 服务器类型 / CPU 架构 / 硬盘类型多维度筛选）")
    @GetMapping("/hardware")
    public Result<HardwareStatisticsDTO> getHardwareStatistics(
            @RequestParam(value = "regions", required = false) List<String> regions,
            @RequestParam(value = "serverTypeIds", required = false) List<Long> serverTypeIds,
            @RequestParam(value = "cpuArchitectures", required = false) List<String> cpuArchitectures,
            @RequestParam(value = "hardDiskTypes", required = false) List<String> hardDiskTypes) {
        return Result.success(hardwareStatisticsService.getHardwareStatistics(
                regions, serverTypeIds, cpuArchitectures, hardDiskTypes));
    }
}

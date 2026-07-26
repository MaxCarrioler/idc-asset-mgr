package com.dcasset.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemoryDTO {

    private Long id;

    @NotNull(message = "服务器ID不能为空")
    private Long serverId;

    @NotNull(message = "内存条数量不能为空")
    @Min(value = 1, message = "内存条数量至少为1")
    private Integer stickCount;

    @NotNull(message = "单条容量不能为空")
    @Min(value = 1, message = "单条容量至少为1GB")
    private Integer capacityPerStickGb;

    @NotNull(message = "内存频率不能为空")
    @Min(value = 1, message = "内存频率至少为1MHz")
    private Integer frequencyMhz;

    @NotBlank(message = "内存型号不能为空")
    private String model;

    private String memoryType;
    private String manufacturer;
    private String slotInfo;
    private String remark;
}

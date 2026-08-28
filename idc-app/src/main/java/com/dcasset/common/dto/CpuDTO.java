package com.dcasset.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CpuDTO {

    private Long id;

    @NotNull(message = "服务器ID不能为空")
    private Long serverId;

    @NotBlank(message = "CPU型号不能为空")
    private String model;

    @NotNull(message = "核数不能为空")
    @Min(value = 1, message = "核数至少为1")
    private Integer cores;

    @NotNull(message = "线程数不能为空")
    @Min(value = 1, message = "线程数至少为1")
    private Integer threads;

    @NotNull(message = "主频不能为空")
    private Double baseFrequencyGhz;

    private Double maxFrequencyGhz;
    private String socket;
    private String manufacturer;
    private Integer quantity;
    private String remark;
}

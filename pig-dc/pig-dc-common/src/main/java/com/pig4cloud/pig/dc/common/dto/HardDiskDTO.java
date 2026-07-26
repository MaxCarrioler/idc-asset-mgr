package com.pig4cloud.pig.dc.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HardDiskDTO {

    private Long id;

    @NotNull(message = "服务器ID不能为空")
    private Long serverId;

    @NotBlank(message = "硬盘型号不能为空")
    private String model;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量至少为1GB")
    private Integer capacityGb;

    @NotBlank(message = "硬盘类型不能为空")
    private String diskType;

    private String interfaceType;
    private Integer rpm;
    private String formFactor;
    private String slotPosition;
    private String serialNumber;
    private String healthStatus;
    private String manufacturer;
    private String remark;
}

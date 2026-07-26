package com.pig4cloud.pig.dc.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HbaCardDTO {

    private Long id;

    @NotNull(message = "服务器ID不能为空")
    private Long serverId;

    @NotBlank(message = "HBA卡型号不能为空")
    private String model;

    @NotNull(message = "端口数不能为空")
    @Min(value = 1, message = "端口数至少为1")
    private Integer portCount;

    @NotNull(message = "速率不能为空")
    private Double speedGbps;

    private String interfaceType;
    private String firmwareVersion;
    private String wwnn;
    private String wwpn;
    private String manufacturer;
    private String pciSlot;
    private String remark;
}

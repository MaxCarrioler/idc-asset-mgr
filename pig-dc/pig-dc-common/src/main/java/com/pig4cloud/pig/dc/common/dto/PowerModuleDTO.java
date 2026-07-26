package com.pig4cloud.pig.dc.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PowerModuleDTO {

    private Long id;

    @NotNull(message = "服务器ID不能为空")
    private Long serverId;

    @NotBlank(message = "电源模块型号不能为空")
    private String model;

    @NotNull(message = "额定功率不能为空")
    @Min(value = 1, message = "额定功率至少为1W")
    private Integer ratedPowerWatt;

    private String efficiencyRating;
    private String inputVoltage;
    private String status;
    private String manufacturer;
    private String serialNumber;
    private String slotPosition;
    private String remark;
}

package com.pig4cloud.pig.dc.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RaidSasCardDTO {

    private Long id;

    @NotNull(message = "服务器ID不能为空")
    private Long serverId;

    @NotBlank(message = "RAID/SAS卡型号不能为空")
    private String model;

    private String raidLevelsSupported;

    @NotNull(message = "缓存大小不能为空")
    @Min(value = 0, message = "缓存大小不能为负")
    private Integer cacheSizeMb;

    @NotNull(message = "端口数不能为空")
    @Min(value = 1, message = "端口数至少为1")
    private Integer portCount;

    private String interfaceType;
    private String firmwareVersion;
    private Boolean batteryBacked;
    private String manufacturer;
    private String pciSlot;
    private String remark;
}

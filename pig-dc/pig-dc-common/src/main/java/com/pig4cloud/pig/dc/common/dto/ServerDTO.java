package com.pig4cloud.pig.dc.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServerDTO {

    private Long id;

    @NotBlank(message = "服务器名称不能为空")
    private String serverName;

    private String ipAddress;
    private String rackLocation;
    private String region;
    private String serverType;
    private String serverModel;
    private String manufacturer;
    private String serialNumber;
    private String status;
    private String osType;
    private String cpuArchitecture;
    private String remark;
    private LocalDateTime updatedAt;

    /** 更新时填写，仅用于记录操作原因，不入库 */
    private String changeReason;
}

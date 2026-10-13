package com.dcasset.common.dto;

import lombok.Data;

@Data
public class SparePartItemDTO {

    private Long id;
    private Long serverId;
    private String serverName;
    private String model;
    private String manufacturer;
    private String serialNumber;
    private String remark;
    /** 规格摘要，如 CPU 核数/线程、内存容量等 */
    private String specSummary;
}

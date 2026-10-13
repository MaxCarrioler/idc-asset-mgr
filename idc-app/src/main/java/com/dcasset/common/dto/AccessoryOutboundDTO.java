package com.dcasset.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccessoryOutboundDTO {

    private Long id;

    private Long requestId;

    private Long sparePartId;

    private String serialNumber;

    private Long categoryId;

    private String categoryName;

    private Long sourceServerId;

    private String sourceServerSerial;

    private String sourceServerName;

    private Long targetServerId;

    private String targetServerName;

    private String targetServerSerial;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime outboundTime;
}

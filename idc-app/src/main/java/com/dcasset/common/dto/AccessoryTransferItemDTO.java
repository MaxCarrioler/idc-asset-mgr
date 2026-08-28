package com.dcasset.common.dto;

import lombok.Data;

@Data
public class AccessoryTransferItemDTO {

    private Long id;

    private Long requestId;

    private Long sparePartId;

    private String serialNumber;

    private String manufacturer;

    private String specSummary;

    private Long sourceServerId;

    private String sourceServerSerial;

    private String sourceServerName;
}

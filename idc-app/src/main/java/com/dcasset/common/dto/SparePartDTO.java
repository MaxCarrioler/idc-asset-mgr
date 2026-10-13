package com.dcasset.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SparePartDTO {

    private Long id;

    @NotNull(message = "备件种类不能为空")
    private Long categoryId;

    private String categoryName;

    private String manufacturer;

    @NotBlank(message = "序列号不能为空")
    private String serialNumber;

    private String specSummary;

    private String location;

    private String remark;

    /** IN_STOCK / RESERVED / OUT */
    private String status;

    private Long sourceServerId;

    private String sourceServerSerial;

    private String sourceServerName;

    /** 现役服务器（出库后对应配件中心目标服务器） */
    private Long activeServerId;

    private String activeServerSerial;

    private String activeServerName;
}

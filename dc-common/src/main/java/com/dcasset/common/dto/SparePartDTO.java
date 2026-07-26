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
}

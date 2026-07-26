package com.pig4cloud.pig.dc.common.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AccessoryTransferApproveRequest {

    @NotEmpty(message = "请选择至少一个备件序列号")
    private List<Long> sparePartIds;

    private String remark;
}

package com.dcasset.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AccessoryTransferRequestDTO {

    private Long id;

    @NotNull(message = "配件种类不能为空")
    private Long categoryId;

    private String categoryName;

    /** 参数/规格描述 */
    private String specParams;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;

    @NotBlank(message = "用途不能为空")
    private String purpose;

    @NotNull(message = "目标服务器不能为空")
    private Long targetServerId;

    private String targetServerName;

    private String targetServerSerial;

    private Long applicantId;

    private String applicantName;

    /** PENDING / APPROVED / REJECTED / OUTBOUND */
    private String status;

    private String rejectReason;

    /** 审批通过备注 */
    private String approveRemark;

    private Long approverId;

    private String approverName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime approvedAt;

    private List<AccessoryTransferItemDTO> items = new ArrayList<>();
}

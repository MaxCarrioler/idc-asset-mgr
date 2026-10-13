package com.dcasset.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparePartSummaryDTO {

    private Long categoryId;

    private String categoryCode;

    private String typeName;

    /** 在库数量（IN_STOCK） */
    private int totalCount;

    /** 记录条数（含预留、已出库） */
    private int recordCount;

    /** 已出库数量（OUT） */
    private int outCount;
}

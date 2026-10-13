package com.dcasset.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparePartTypeSummaryDTO {

    private String type;

    private String label;

    /** 在库数量（IN_STOCK） */
    private int inStockCount;

    /** 已出库数量（OUT） */
    private int outCount;

    /** 该类型下的种类数 */
    private int categoryCount;
}

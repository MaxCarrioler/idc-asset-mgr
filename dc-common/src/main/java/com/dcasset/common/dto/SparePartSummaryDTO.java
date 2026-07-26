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

    /** 库存数量（quantity 累加） */
    private int totalCount;

    /** 记录条数 */
    private int recordCount;
}

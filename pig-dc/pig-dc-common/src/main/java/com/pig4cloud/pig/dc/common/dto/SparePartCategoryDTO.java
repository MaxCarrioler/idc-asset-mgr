package com.pig4cloud.pig.dc.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SparePartCategoryDTO {

    private Long id;

    /** 种类编码，唯一，创建后不可改 */
    private String code;

    private String name;

    private String description;

    private Boolean builtIn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.pig4cloud.pig.dc.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CpuArchitectureDTO {

    private Long id;

    /** 架构编码，唯一标识，创建后不可修改 */
    private String code;

    /** 架构名称 */
    private String name;

    private String description;

    /** 是否内置（不可删除） */
    private Boolean builtIn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

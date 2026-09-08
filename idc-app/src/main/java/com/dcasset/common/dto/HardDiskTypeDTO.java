package com.dcasset.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HardDiskTypeDTO {

    private Long id;

    /** 硬盘类型编码，唯一标识，创建后不可修改 */
    private String code;

    /** 硬盘类型名称 */
    private String name;

    private String description;

    /** 是否内置（不可删除） */
    private Boolean builtIn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

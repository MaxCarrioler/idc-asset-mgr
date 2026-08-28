package com.dcasset.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServerTypeDTO {

    private Long id;

    /** 类型编码，唯一标识，创建后不可修改 */
    private String code;

    /** 类型名称 */
    private String name;

    private String description;

    /** 是否统计 CPU 线程数 */
    private Boolean trackThreads;

    /** 是否统计硬盘容量及规格汇总 */
    private Boolean includeDiskStats;

    /** 是否内置类型（不可删除） */
    private Boolean builtIn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

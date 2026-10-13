package com.dcasset.accessory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_spare_part", uniqueConstraints = {
        @UniqueConstraint(name = "uk_spare_part_category_serial", columnNames = {"categoryId", "serialNumber"})
})
public class SparePartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long categoryId;

    @Column(length = 100)
    private String manufacturer;

    @Column(nullable = false, length = 100)
    private String serialNumber;

    @Column(length = 500)
    private String specSummary;

    @Column(length = 100)
    private String location;

    @Column(length = 500)
    private String remark;

    /** 备件来源服务器 ID（「无」「采购」时为空） */
    private Long sourceServerId;

    /** 备件来源服务器序列号（可为「无」「采购」或真实序列号） */
    @Column(length = 100)
    private String sourceServerSerial;

    /** 备件来源服务器名称（真实服务器时写入） */
    @Column(length = 200)
    private String sourceServerName;

    /** 现役服务器 ID（出库后写入目标服务器） */
    private Long activeServerId;

    /** 现役服务器序列号 */
    @Column(length = 100)
    private String activeServerSerial;

    /** 现役服务器名称 */
    @Column(length = 200)
    private String activeServerName;

    /** IN_STOCK / RESERVED / OUT */
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'IN_STOCK'")
    private String status = "IN_STOCK";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null || status.isBlank()) {
            status = "IN_STOCK";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

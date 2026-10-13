package com.dcasset.accessory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_accessory_outbound")
public class AccessoryOutboundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requestId;

    @Column(nullable = false)
    private Long sparePartId;

    @Column(nullable = false, length = 100)
    private String serialNumber;

    private Long categoryId;

    /** 来源服务器 —— 出库强制要求 */
    private Long sourceServerId;

    @Column(nullable = false, length = 100)
    private String sourceServerSerial;

    @Column(length = 100)
    private String sourceServerName;

    private Long targetServerId;

    @Column(length = 100)
    private String targetServerName;

    @Column(length = 100)
    private String targetServerSerial;

    private Long operatorId;

    @Column(length = 64)
    private String operatorName;

    @Column(length = 500)
    private String remark;

    private LocalDateTime outboundTime;

    @PrePersist
    public void prePersist() {
        if (outboundTime == null) {
            outboundTime = LocalDateTime.now();
        }
    }
}

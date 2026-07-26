package com.pig4cloud.pig.dc.accessory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_accessory_transfer_request")
public class AccessoryTransferRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long categoryId;

    @Column(length = 500)
    private String specParams;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 500)
    private String purpose;

    @Column(nullable = false)
    private Long targetServerId;

    @Column(length = 100)
    private String targetServerName;

    @Column(length = 100)
    private String targetServerSerial;

    private Long applicantId;

    @Column(length = 64)
    private String applicantName;

    /** PENDING / APPROVED / REJECTED / OUTBOUND */
    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String rejectReason;

    private Long approverId;

    @Column(length = 64)
    private String approverName;

    private LocalDateTime approvedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

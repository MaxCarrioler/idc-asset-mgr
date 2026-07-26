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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

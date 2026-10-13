package com.dcasset.accessory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_hard_disk")
public class HardDiskEntity extends BaseAccessoryEntity {

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private Integer capacityGb;

    @Column(nullable = false, length = 30)
    private String diskType;

    private String interfaceType;
    private Integer rpm;
    private String formFactor;
    private String slotPosition;
    private String serialNumber;
    private String healthStatus;
    private String manufacturer;
}

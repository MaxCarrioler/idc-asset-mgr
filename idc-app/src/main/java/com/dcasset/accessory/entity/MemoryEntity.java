package com.dcasset.accessory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_memory")
public class MemoryEntity extends BaseAccessoryEntity {

    @Column(nullable = false)
    private Integer stickCount;

    @Column(nullable = false)
    private Integer capacityPerStickGb;

    @Column(nullable = false)
    private Integer frequencyMhz;

    @Column(nullable = false, length = 100)
    private String model;

    private String memoryType;
    private String manufacturer;
    private String slotInfo;
}

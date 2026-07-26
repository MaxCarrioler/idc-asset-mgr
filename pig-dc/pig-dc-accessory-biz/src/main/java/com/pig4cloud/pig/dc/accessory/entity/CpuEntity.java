package com.pig4cloud.pig.dc.accessory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_cpu")
public class CpuEntity extends BaseAccessoryEntity {

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private Integer cores;

    @Column(nullable = false)
    private Integer threads;

    @Column(nullable = false)
    private Double baseFrequencyGhz;

    private Double maxFrequencyGhz;
    private String socket;
    private String manufacturer;
    private Integer quantity;
}

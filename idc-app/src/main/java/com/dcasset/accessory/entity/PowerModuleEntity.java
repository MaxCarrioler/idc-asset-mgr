package com.dcasset.accessory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_power_module")
public class PowerModuleEntity extends BaseAccessoryEntity {

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private Integer ratedPowerWatt;

    private String efficiencyRating;
    private String inputVoltage;
    private String status;
    private String manufacturer;
    private String serialNumber;
    private String slotPosition;
}

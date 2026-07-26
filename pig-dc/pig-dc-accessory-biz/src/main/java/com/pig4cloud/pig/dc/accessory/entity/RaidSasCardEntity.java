package com.pig4cloud.pig.dc.accessory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_raid_sas_card")
public class RaidSasCardEntity extends BaseAccessoryEntity {

    @Column(nullable = false, length = 100)
    private String model;

    private String raidLevelsSupported;

    @Column(nullable = false)
    private Integer cacheSizeMb;

    @Column(nullable = false)
    private Integer portCount;

    private String interfaceType;
    private String firmwareVersion;
    private Boolean batteryBacked;
    private String manufacturer;
    private String pciSlot;
}

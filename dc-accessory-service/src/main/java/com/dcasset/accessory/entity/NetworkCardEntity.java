package com.dcasset.accessory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_network_card")
public class NetworkCardEntity extends BaseAccessoryEntity {

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private Integer portCount;

    @Column(nullable = false)
    private Double speedGbps;

    private String interfaceType;
    private String macAddress;
    private String manufacturer;
    private String pciSlot;
    private String driverVersion;
}

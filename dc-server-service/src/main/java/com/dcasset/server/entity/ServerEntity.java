package com.dcasset.server.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_server")
public class ServerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String serverName;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 50)
    private String rackLocation;

    @Column(length = 100)
    private String region;

    @Column(length = 30)
    private String serverType;

    @Column(length = 100)
    private String serverModel;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String serialNumber;

    @Column(length = 30)
    private String status;

    @Column(length = 50)
    private String osType;

    @Column(length = 30)
    private String cpuArchitecture;

    @Column(length = 500)
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "RUNNING";
        }
        if (serverType == null) {
            serverType = "COMPUTE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

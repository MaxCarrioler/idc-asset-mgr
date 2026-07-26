package com.dcasset.server.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_server_operation_record")
public class ServerOperationRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long serverId;

    private LocalDateTime modifiedAt;

    @Column(length = 2000)
    private String changeContent;

    @Column(length = 500)
    private String changeReason;

    @PrePersist
    public void prePersist() {
        if (modifiedAt == null) {
            modifiedAt = LocalDateTime.now();
        }
    }
}

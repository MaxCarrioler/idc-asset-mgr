package com.pig4cloud.pig.dc.server.repository;

import com.pig4cloud.pig.dc.server.entity.ServerOperationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServerOperationRecordRepository extends JpaRepository<ServerOperationRecordEntity, Long> {

    List<ServerOperationRecordEntity> findByServerIdOrderByModifiedAtDesc(Long serverId);

    long countByServerId(Long serverId);
}

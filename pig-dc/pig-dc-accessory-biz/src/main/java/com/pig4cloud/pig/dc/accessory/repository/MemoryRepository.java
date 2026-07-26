package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.MemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoryRepository extends JpaRepository<MemoryEntity, Long> {
    List<MemoryEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

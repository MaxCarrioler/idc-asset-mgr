package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.MemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoryRepository extends JpaRepository<MemoryEntity, Long> {
    List<MemoryEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

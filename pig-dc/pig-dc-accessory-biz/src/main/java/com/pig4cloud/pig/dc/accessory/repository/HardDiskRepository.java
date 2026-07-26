package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.HardDiskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HardDiskRepository extends JpaRepository<HardDiskEntity, Long> {
    List<HardDiskEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

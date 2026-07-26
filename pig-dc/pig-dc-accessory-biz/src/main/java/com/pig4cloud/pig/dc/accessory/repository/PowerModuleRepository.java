package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.PowerModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PowerModuleRepository extends JpaRepository<PowerModuleEntity, Long> {
    List<PowerModuleEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

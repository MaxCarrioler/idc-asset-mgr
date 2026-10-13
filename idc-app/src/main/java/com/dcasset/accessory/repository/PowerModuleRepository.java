package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.PowerModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PowerModuleRepository extends JpaRepository<PowerModuleEntity, Long> {
    List<PowerModuleEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

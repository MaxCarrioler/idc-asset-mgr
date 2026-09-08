package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.HardDiskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HardDiskRepository extends JpaRepository<HardDiskEntity, Long> {
    List<HardDiskEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);

    long countByDiskType(String diskType);
}

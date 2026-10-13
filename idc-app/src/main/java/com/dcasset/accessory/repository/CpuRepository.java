package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.CpuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CpuRepository extends JpaRepository<CpuEntity, Long> {
    List<CpuEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

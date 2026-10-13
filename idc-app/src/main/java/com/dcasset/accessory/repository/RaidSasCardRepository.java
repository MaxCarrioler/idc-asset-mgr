package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.RaidSasCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaidSasCardRepository extends JpaRepository<RaidSasCardEntity, Long> {
    List<RaidSasCardEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

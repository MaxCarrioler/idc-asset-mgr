package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.RaidSasCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RaidSasCardRepository extends JpaRepository<RaidSasCardEntity, Long> {
    List<RaidSasCardEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

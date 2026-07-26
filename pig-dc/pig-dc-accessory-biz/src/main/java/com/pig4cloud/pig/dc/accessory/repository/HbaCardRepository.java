package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.HbaCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HbaCardRepository extends JpaRepository<HbaCardEntity, Long> {
    List<HbaCardEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.NetworkCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NetworkCardRepository extends JpaRepository<NetworkCardEntity, Long> {
    List<NetworkCardEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

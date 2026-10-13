package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.NetworkCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NetworkCardRepository extends JpaRepository<NetworkCardEntity, Long> {
    List<NetworkCardEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

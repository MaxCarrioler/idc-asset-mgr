package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.HbaCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HbaCardRepository extends JpaRepository<HbaCardEntity, Long> {
    List<HbaCardEntity> findByServerId(Long serverId);
    void deleteByServerId(Long serverId);
}

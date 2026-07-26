package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.AccessoryTransferItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccessoryTransferItemRepository extends JpaRepository<AccessoryTransferItemEntity, Long> {

    List<AccessoryTransferItemEntity> findByRequestId(Long requestId);

    void deleteByRequestId(Long requestId);
}

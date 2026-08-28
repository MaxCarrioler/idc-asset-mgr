package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.AccessoryOutboundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccessoryOutboundRepository extends JpaRepository<AccessoryOutboundEntity, Long> {

    List<AccessoryOutboundEntity> findAllByOrderByOutboundTimeDesc();

    List<AccessoryOutboundEntity> findByRequestId(Long requestId);

    Optional<AccessoryOutboundEntity> findFirstBySparePartIdOrderByOutboundTimeDesc(Long sparePartId);

    List<AccessoryOutboundEntity> findBySparePartIdInOrderByOutboundTimeDesc(Collection<Long> sparePartIds);
}

package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.AccessoryTransferRequestEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccessoryTransferRequestRepository extends JpaRepository<AccessoryTransferRequestEntity, Long> {

    List<AccessoryTransferRequestEntity> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    List<AccessoryTransferRequestEntity> findAllByOrderByCreatedAtDesc();

    List<AccessoryTransferRequestEntity> findByStatusOrderByCreatedAtDesc(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AccessoryTransferRequestEntity r where r.id = :id")
    Optional<AccessoryTransferRequestEntity> findByIdForUpdate(@Param("id") Long id);
}

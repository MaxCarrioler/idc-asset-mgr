package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.SparePartEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SparePartRepository extends JpaRepository<SparePartEntity, Long> {

    List<SparePartEntity> findByCategoryIdOrderByIdAsc(Long categoryId);

    long countByCategoryId(Long categoryId);

    boolean existsByCategoryIdAndSerialNumber(Long categoryId, String serialNumber);

    Optional<SparePartEntity> findByCategoryIdAndSerialNumber(Long categoryId, String serialNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from SparePartEntity p where p.id = :id")
    Optional<SparePartEntity> findByIdForUpdate(@Param("id") Long id);
}

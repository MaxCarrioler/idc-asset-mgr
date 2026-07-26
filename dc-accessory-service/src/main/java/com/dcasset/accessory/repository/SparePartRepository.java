package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.SparePartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SparePartRepository extends JpaRepository<SparePartEntity, Long> {

    List<SparePartEntity> findByCategoryIdOrderByIdAsc(Long categoryId);

    long countByCategoryId(Long categoryId);

    boolean existsByCategoryIdAndSerialNumber(Long categoryId, String serialNumber);

    Optional<SparePartEntity> findByCategoryIdAndSerialNumber(Long categoryId, String serialNumber);
}

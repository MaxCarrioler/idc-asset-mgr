package com.pig4cloud.pig.dc.accessory.repository;

import com.pig4cloud.pig.dc.accessory.entity.SparePartCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SparePartCategoryRepository extends JpaRepository<SparePartCategoryEntity, Long> {

    Optional<SparePartCategoryEntity> findByCode(String code);

    boolean existsByCode(String code);
}

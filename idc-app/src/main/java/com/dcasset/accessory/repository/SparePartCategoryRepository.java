package com.dcasset.accessory.repository;

import com.dcasset.accessory.entity.SparePartCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SparePartCategoryRepository extends JpaRepository<SparePartCategoryEntity, Long> {

    Optional<SparePartCategoryEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<SparePartCategoryEntity> findByAccessoryTypeOrderByIdAsc(String accessoryType);
}

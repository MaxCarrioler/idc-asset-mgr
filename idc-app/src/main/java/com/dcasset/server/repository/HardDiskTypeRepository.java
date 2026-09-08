package com.dcasset.server.repository;

import com.dcasset.server.entity.HardDiskTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HardDiskTypeRepository extends JpaRepository<HardDiskTypeEntity, Long> {

    Optional<HardDiskTypeEntity> findByCode(String code);

    boolean existsByCode(String code);
}

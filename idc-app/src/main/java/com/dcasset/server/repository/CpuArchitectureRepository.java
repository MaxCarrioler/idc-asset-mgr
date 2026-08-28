package com.dcasset.server.repository;

import com.dcasset.server.entity.CpuArchitectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CpuArchitectureRepository extends JpaRepository<CpuArchitectureEntity, Long> {

    Optional<CpuArchitectureEntity> findByCode(String code);

    boolean existsByCode(String code);
}

package com.pig4cloud.pig.dc.server.repository;

import com.pig4cloud.pig.dc.server.entity.CpuArchitectureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CpuArchitectureRepository extends JpaRepository<CpuArchitectureEntity, Long> {

    Optional<CpuArchitectureEntity> findByCode(String code);

    boolean existsByCode(String code);
}

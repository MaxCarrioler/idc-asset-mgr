package com.dcasset.server.repository;

import com.dcasset.server.entity.ServerTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerTypeRepository extends JpaRepository<ServerTypeEntity, Long> {

    Optional<ServerTypeEntity> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}

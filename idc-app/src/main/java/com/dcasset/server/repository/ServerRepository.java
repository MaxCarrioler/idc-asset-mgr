package com.dcasset.server.repository;

import com.dcasset.server.entity.ServerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServerRepository extends JpaRepository<ServerEntity, Long> {

    long countByServerType(String serverType);

    long countByCpuArchitecture(String cpuArchitecture);

    @Query("SELECT DISTINCT s.region FROM ServerEntity s WHERE s.region IS NOT NULL AND s.region <> '' ORDER BY s.region")
    List<String> findDistinctRegions();

    @Query("SELECT DISTINCT s.cpuArchitecture FROM ServerEntity s WHERE s.cpuArchitecture IS NOT NULL AND s.cpuArchitecture <> '' ORDER BY s.cpuArchitecture")
    List<String> findDistinctCpuArchitectures();

    @Query("SELECT s FROM ServerEntity s WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "s.serverName LIKE %:keyword% OR " +
           "s.ipAddress LIKE %:keyword% OR s.serverModel LIKE %:keyword% OR " +
           "s.rackLocation LIKE %:keyword% OR s.region LIKE %:keyword% OR " +
           "s.serialNumber LIKE %:keyword% OR s.cpuArchitecture LIKE %:keyword%)")
    Page<ServerEntity> search(@Param("keyword") String keyword, Pageable pageable);
}

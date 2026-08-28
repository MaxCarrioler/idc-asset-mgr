package com.dcasset.admin.repository;

import com.dcasset.admin.entity.SysRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SysRoleRepository extends JpaRepository<SysRoleEntity, Long> {

    List<SysRoleEntity> findByDelFlagOrderByRoleIdAsc(String delFlag);

    Optional<SysRoleEntity> findByRoleCodeAndDelFlag(String roleCode, String delFlag);
}

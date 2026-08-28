package com.dcasset.admin.repository;

import com.dcasset.admin.entity.SysUserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysUserRoleRepository extends JpaRepository<SysUserRoleEntity, SysUserRoleEntity.PK> {

    List<SysUserRoleEntity> findByUserId(Long userId);

    List<SysUserRoleEntity> findByRoleId(Long roleId);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);
}

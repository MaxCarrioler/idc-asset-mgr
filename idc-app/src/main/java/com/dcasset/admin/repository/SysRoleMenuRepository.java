package com.dcasset.admin.repository;

import com.dcasset.admin.entity.SysRoleMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenuEntity, SysRoleMenuEntity.PK> {

    List<SysRoleMenuEntity> findByRoleId(Long roleId);

    List<SysRoleMenuEntity> findByRoleIdIn(List<Long> roleIds);

    @Modifying
    @Transactional
    void deleteByRoleId(Long roleId);
}

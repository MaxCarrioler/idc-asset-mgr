package com.dcasset.admin.repository;

import com.dcasset.admin.entity.SysMenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SysMenuRepository extends JpaRepository<SysMenuEntity, Long> {

    List<SysMenuEntity> findByDelFlagOrderBySortOrderAsc(String delFlag);
}

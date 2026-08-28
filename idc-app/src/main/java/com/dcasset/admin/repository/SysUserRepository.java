package com.dcasset.admin.repository;

import com.dcasset.admin.entity.SysUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SysUserRepository extends JpaRepository<SysUserEntity, Long>,
        JpaSpecificationExecutor<SysUserEntity> {

    Optional<SysUserEntity> findByUsernameAndDelFlag(String username, String delFlag);

    boolean existsByUsernameAndDelFlag(String username, String delFlag);

    List<SysUserEntity> findByDelFlag(String delFlag);
}

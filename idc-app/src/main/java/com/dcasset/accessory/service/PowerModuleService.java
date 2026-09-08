package com.dcasset.accessory.service;

import com.dcasset.common.dto.PowerModuleDTO;
import com.dcasset.accessory.entity.PowerModuleEntity;
import com.dcasset.accessory.repository.PowerModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PowerModuleService extends BaseAccessoryService<PowerModuleEntity, PowerModuleDTO> {

    private final PowerModuleRepository powerModuleRepository;

    @Override
    protected JpaRepository<PowerModuleEntity, Long> getRepository() {
        return powerModuleRepository;
    }

    @Override
    protected Supplier<PowerModuleEntity> entitySupplier() {
        return PowerModuleEntity::new;
    }

    @Override
    protected void copyDtoToEntity(PowerModuleDTO dto, PowerModuleEntity entity) {
        // 排除 id：编辑时前端不传 id，避免把已加载实体的主键置空导致误判为新增
        BeanUtils.copyProperties(dto, entity, "id");
    }

    @Override
    protected void copyEntityToDto(PowerModuleEntity entity, PowerModuleDTO dto) {
        BeanUtils.copyProperties(entity, dto);
    }

    @Override
    protected List<PowerModuleEntity> findByServerId(Long serverId) {
        return powerModuleRepository.findByServerId(serverId);
    }

    @Override
    protected String notFoundMessage(Long id) {
        return "电源模块配件不存在: " + id;
    }

    @Override
    protected PowerModuleDTO newDto() {
        return new PowerModuleDTO();
    }

    @Override
    protected void setIdNull(PowerModuleEntity entity) {
        entity.setId(null);
    }
}

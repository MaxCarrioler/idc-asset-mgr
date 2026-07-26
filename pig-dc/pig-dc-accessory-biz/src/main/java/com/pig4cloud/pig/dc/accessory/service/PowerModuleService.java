package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.dc.common.dto.PowerModuleDTO;
import com.pig4cloud.pig.dc.accessory.entity.PowerModuleEntity;
import com.pig4cloud.pig.dc.accessory.repository.PowerModuleRepository;
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
        BeanUtils.copyProperties(dto, entity);
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

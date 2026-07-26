package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.dc.common.dto.CpuDTO;
import com.pig4cloud.pig.dc.accessory.entity.CpuEntity;
import com.pig4cloud.pig.dc.accessory.repository.CpuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CpuService extends BaseAccessoryService<CpuEntity, CpuDTO> {

    private final CpuRepository cpuRepository;

    @Override
    protected JpaRepository<CpuEntity, Long> getRepository() {
        return cpuRepository;
    }

    @Override
    protected Supplier<CpuEntity> entitySupplier() {
        return CpuEntity::new;
    }

    @Override
    protected void copyDtoToEntity(CpuDTO dto, CpuEntity entity) {
        BeanUtils.copyProperties(dto, entity);
    }

    @Override
    protected void copyEntityToDto(CpuEntity entity, CpuDTO dto) {
        BeanUtils.copyProperties(entity, dto);
    }

    @Override
    protected List<CpuEntity> findByServerId(Long serverId) {
        return cpuRepository.findByServerId(serverId);
    }

    @Override
    protected String notFoundMessage(Long id) {
        return "CPU配件不存在: " + id;
    }

    @Override
    protected CpuDTO newDto() {
        return new CpuDTO();
    }

    @Override
    protected void setIdNull(CpuEntity entity) {
        entity.setId(null);
    }
}

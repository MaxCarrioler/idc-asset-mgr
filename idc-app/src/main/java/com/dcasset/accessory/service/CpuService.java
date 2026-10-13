package com.dcasset.accessory.service;

import com.dcasset.common.dto.CpuDTO;
import com.dcasset.accessory.entity.CpuEntity;
import com.dcasset.accessory.repository.CpuRepository;
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
        // 排除 id：编辑时前端不传 id，避免把已加载实体的主键置空导致误判为新增
        BeanUtils.copyProperties(dto, entity, "id");
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
        entity.setId((Long) null);
    }
}

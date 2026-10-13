package com.dcasset.accessory.service;

import com.dcasset.common.dto.HardDiskDTO;
import com.dcasset.accessory.entity.HardDiskEntity;
import com.dcasset.accessory.repository.HardDiskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class HardDiskService extends BaseAccessoryService<HardDiskEntity, HardDiskDTO> {

    private final HardDiskRepository hardDiskRepository;

    @Override
    protected JpaRepository<HardDiskEntity, Long> getRepository() {
        return hardDiskRepository;
    }

    @Override
    protected Supplier<HardDiskEntity> entitySupplier() {
        return HardDiskEntity::new;
    }

    @Override
    protected void copyDtoToEntity(HardDiskDTO dto, HardDiskEntity entity) {
        // 排除 id：编辑时前端不传 id，避免把已加载实体的主键置空导致误判为新增
        BeanUtils.copyProperties(dto, entity, "id");
    }

    @Override
    protected void copyEntityToDto(HardDiskEntity entity, HardDiskDTO dto) {
        BeanUtils.copyProperties(entity, dto);
    }

    @Override
    protected List<HardDiskEntity> findByServerId(Long serverId) {
        return hardDiskRepository.findByServerId(serverId);
    }

    @Override
    protected String notFoundMessage(Long id) {
        return "硬盘配件不存在: " + id;
    }

    @Override
    protected HardDiskDTO newDto() {
        return new HardDiskDTO();
    }

    @Override
    protected void setIdNull(HardDiskEntity entity) {
        entity.setId((Long) null);
    }
}

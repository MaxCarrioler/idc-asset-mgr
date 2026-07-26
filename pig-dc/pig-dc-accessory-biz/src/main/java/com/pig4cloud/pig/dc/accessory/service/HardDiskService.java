package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.dc.common.dto.HardDiskDTO;
import com.pig4cloud.pig.dc.accessory.entity.HardDiskEntity;
import com.pig4cloud.pig.dc.accessory.repository.HardDiskRepository;
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
        BeanUtils.copyProperties(dto, entity);
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
        entity.setId(null);
    }
}

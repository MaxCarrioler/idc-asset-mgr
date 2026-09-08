package com.dcasset.accessory.service;

import com.dcasset.common.dto.MemoryDTO;
import com.dcasset.accessory.entity.MemoryEntity;
import com.dcasset.accessory.repository.MemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class MemoryService extends BaseAccessoryService<MemoryEntity, MemoryDTO> {

    private final MemoryRepository memoryRepository;

    @Override
    protected JpaRepository<MemoryEntity, Long> getRepository() {
        return memoryRepository;
    }

    @Override
    protected Supplier<MemoryEntity> entitySupplier() {
        return MemoryEntity::new;
    }

    @Override
    protected void copyDtoToEntity(MemoryDTO dto, MemoryEntity entity) {
        // 排除 id：编辑时前端不传 id，避免把已加载实体的主键置空导致误判为新增
        BeanUtils.copyProperties(dto, entity, "id");
    }

    @Override
    protected void copyEntityToDto(MemoryEntity entity, MemoryDTO dto) {
        BeanUtils.copyProperties(entity, dto);
    }

    @Override
    protected List<MemoryEntity> findByServerId(Long serverId) {
        return memoryRepository.findByServerId(serverId);
    }

    @Override
    protected String notFoundMessage(Long id) {
        return "内存配件不存在: " + id;
    }

    @Override
    protected MemoryDTO newDto() {
        return new MemoryDTO();
    }

    @Override
    protected void setIdNull(MemoryEntity entity) {
        entity.setId(null);
    }
}

package com.dcasset.accessory.service;

import com.dcasset.common.dto.HbaCardDTO;
import com.dcasset.accessory.entity.HbaCardEntity;
import com.dcasset.accessory.repository.HbaCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class HbaCardService extends BaseAccessoryService<HbaCardEntity, HbaCardDTO> {

    private final HbaCardRepository hbaCardRepository;

    @Override
    protected JpaRepository<HbaCardEntity, Long> getRepository() {
        return hbaCardRepository;
    }

    @Override
    protected Supplier<HbaCardEntity> entitySupplier() {
        return HbaCardEntity::new;
    }

    @Override
    protected void copyDtoToEntity(HbaCardDTO dto, HbaCardEntity entity) {
        // 排除 id：编辑时前端不传 id，避免把已加载实体的主键置空导致误判为新增
        BeanUtils.copyProperties(dto, entity, "id");
    }

    @Override
    protected void copyEntityToDto(HbaCardEntity entity, HbaCardDTO dto) {
        BeanUtils.copyProperties(entity, dto);
    }

    @Override
    protected List<HbaCardEntity> findByServerId(Long serverId) {
        return hbaCardRepository.findByServerId(serverId);
    }

    @Override
    protected String notFoundMessage(Long id) {
        return "HBA卡配件不存在: " + id;
    }

    @Override
    protected HbaCardDTO newDto() {
        return new HbaCardDTO();
    }

    @Override
    protected void setIdNull(HbaCardEntity entity) {
        entity.setId(null);
    }
}

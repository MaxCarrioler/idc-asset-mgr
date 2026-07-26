package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.dc.common.dto.HbaCardDTO;
import com.pig4cloud.pig.dc.accessory.entity.HbaCardEntity;
import com.pig4cloud.pig.dc.accessory.repository.HbaCardRepository;
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
        BeanUtils.copyProperties(dto, entity);
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

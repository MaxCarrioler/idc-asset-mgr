package com.dcasset.accessory.service;

import com.dcasset.common.dto.RaidSasCardDTO;
import com.dcasset.accessory.entity.RaidSasCardEntity;
import com.dcasset.accessory.repository.RaidSasCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RaidSasCardService extends BaseAccessoryService<RaidSasCardEntity, RaidSasCardDTO> {

    private final RaidSasCardRepository raidSasCardRepository;

    @Override
    protected JpaRepository<RaidSasCardEntity, Long> getRepository() {
        return raidSasCardRepository;
    }

    @Override
    protected Supplier<RaidSasCardEntity> entitySupplier() {
        return RaidSasCardEntity::new;
    }

    @Override
    protected void copyDtoToEntity(RaidSasCardDTO dto, RaidSasCardEntity entity) {
        // 排除 id：编辑时前端不传 id，避免把已加载实体的主键置空导致误判为新增
        BeanUtils.copyProperties(dto, entity, "id");
    }

    @Override
    protected void copyEntityToDto(RaidSasCardEntity entity, RaidSasCardDTO dto) {
        BeanUtils.copyProperties(entity, dto);
    }

    @Override
    protected List<RaidSasCardEntity> findByServerId(Long serverId) {
        return raidSasCardRepository.findByServerId(serverId);
    }

    @Override
    protected String notFoundMessage(Long id) {
        return "RAID/SAS卡配件不存在: " + id;
    }

    @Override
    protected RaidSasCardDTO newDto() {
        return new RaidSasCardDTO();
    }

    @Override
    protected void setIdNull(RaidSasCardEntity entity) {
        entity.setId((Long) null);
    }
}

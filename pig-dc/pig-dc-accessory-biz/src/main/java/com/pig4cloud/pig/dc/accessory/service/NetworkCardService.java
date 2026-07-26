package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.dc.common.dto.NetworkCardDTO;
import com.pig4cloud.pig.dc.accessory.entity.NetworkCardEntity;
import com.pig4cloud.pig.dc.accessory.repository.NetworkCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class NetworkCardService extends BaseAccessoryService<NetworkCardEntity, NetworkCardDTO> {

    private final NetworkCardRepository networkCardRepository;

    @Override
    protected JpaRepository<NetworkCardEntity, Long> getRepository() {
        return networkCardRepository;
    }

    @Override
    protected Supplier<NetworkCardEntity> entitySupplier() {
        return NetworkCardEntity::new;
    }

    @Override
    protected void copyDtoToEntity(NetworkCardDTO dto, NetworkCardEntity entity) {
        BeanUtils.copyProperties(dto, entity);
    }

    @Override
    protected void copyEntityToDto(NetworkCardEntity entity, NetworkCardDTO dto) {
        BeanUtils.copyProperties(entity, dto);
    }

    @Override
    protected List<NetworkCardEntity> findByServerId(Long serverId) {
        return networkCardRepository.findByServerId(serverId);
    }

    @Override
    protected String notFoundMessage(Long id) {
        return "网卡配件不存在: " + id;
    }

    @Override
    protected NetworkCardDTO newDto() {
        return new NetworkCardDTO();
    }

    @Override
    protected void setIdNull(NetworkCardEntity entity) {
        entity.setId(null);
    }
}

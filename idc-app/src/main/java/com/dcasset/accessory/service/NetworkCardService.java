package com.dcasset.accessory.service;

import com.dcasset.common.dto.NetworkCardDTO;
import com.dcasset.accessory.entity.NetworkCardEntity;
import com.dcasset.accessory.repository.NetworkCardRepository;
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
        // 排除 id：编辑时前端不传 id，避免把已加载实体的主键置空导致误判为新增
        BeanUtils.copyProperties(dto, entity, "id");
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

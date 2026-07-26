package com.dcasset.server.service;

import com.dcasset.common.dto.ServerTypeDTO;
import com.dcasset.server.entity.ServerTypeEntity;
import com.dcasset.server.repository.ServerRepository;
import com.dcasset.server.repository.ServerTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerTypeServiceImpl implements ServerTypeService {

    private final ServerTypeRepository serverTypeRepository;
    private final ServerRepository serverRepository;

    @Override
    public List<ServerTypeDTO> list() {
        return serverTypeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ServerTypeDTO getById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    public ServerTypeDTO getByCode(String code) {
        return serverTypeRepository.findByCode(normalizeCode(code))
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("服务器类型不存在: " + code));
    }

    @Override
    @Transactional
    public ServerTypeDTO create(ServerTypeDTO dto) {
        String code = normalizeCode(dto.getCode());
        if (code.isBlank()) {
            throw new IllegalArgumentException("类型编码不能为空");
        }
        if (serverTypeRepository.existsByCode(code)) {
            throw new IllegalArgumentException("类型编码已存在: " + code);
        }
        validateName(dto.getName());

        ServerTypeEntity entity = new ServerTypeEntity();
        entity.setCode(code);
        entity.setName(dto.getName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setTrackThreads(Boolean.TRUE.equals(dto.getTrackThreads()));
        entity.setIncludeDiskStats(Boolean.TRUE.equals(dto.getIncludeDiskStats()));
        entity.setBuiltIn(false);
        return toDto(serverTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public ServerTypeDTO update(Long id, ServerTypeDTO dto) {
        ServerTypeEntity entity = findEntity(id);
        validateName(dto.getName());
        entity.setName(dto.getName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setTrackThreads(Boolean.TRUE.equals(dto.getTrackThreads()));
        entity.setIncludeDiskStats(Boolean.TRUE.equals(dto.getIncludeDiskStats()));
        return toDto(serverTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ServerTypeEntity entity = findEntity(id);
        if (Boolean.TRUE.equals(entity.getBuiltIn())) {
            throw new IllegalArgumentException("内置类型不可删除: " + entity.getName());
        }
        long usageCount = serverRepository.countByServerType(entity.getCode());
        if (usageCount > 0) {
            throw new IllegalArgumentException("该类型下仍有 " + usageCount + " 台服务器，无法删除");
        }
        serverTypeRepository.delete(entity);
    }

    private ServerTypeEntity findEntity(Long id) {
        return serverTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("服务器类型不存在: " + id));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("类型名称不能为空");
        }
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ServerTypeDTO toDto(ServerTypeEntity entity) {
        ServerTypeDTO dto = new ServerTypeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}

package com.dcasset.server.service;

import com.dcasset.common.dto.CpuArchitectureDTO;
import com.dcasset.security.CurrentUser;
import com.dcasset.security.SecurityUtils;
import com.dcasset.server.entity.CpuArchitectureEntity;
import com.dcasset.server.repository.CpuArchitectureRepository;
import com.dcasset.server.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CpuArchitectureServiceImpl implements CpuArchitectureService {

    private final CpuArchitectureRepository cpuArchitectureRepository;
    private final ServerRepository serverRepository;

    @Override
    public List<CpuArchitectureDTO> list() {
        return cpuArchitectureRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CpuArchitectureDTO getById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    public CpuArchitectureDTO getByCode(String code) {
        return cpuArchitectureRepository.findByCode(normalizeCode(code))
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("CPU 架构不存在: " + code));
    }

    @Override
    @Transactional
    public CpuArchitectureDTO create(CpuArchitectureDTO dto) {
        String code = normalizeCode(dto.getCode());
        if (code.isBlank()) {
            throw new IllegalArgumentException("架构编码不能为空");
        }
        if (cpuArchitectureRepository.existsByCode(code)) {
            throw new IllegalArgumentException("架构编码已存在: " + code);
        }
        validateName(dto.getName());

        CpuArchitectureEntity entity = new CpuArchitectureEntity();
        entity.setCode(code);
        entity.setName(dto.getName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setBuiltIn(false);
        return toDto(cpuArchitectureRepository.save(entity));
    }

    @Override
    @Transactional
    public CpuArchitectureDTO update(Long id, CpuArchitectureDTO dto) {
        CpuArchitectureEntity entity = findEntity(id);
        validateName(dto.getName());
        entity.setName(dto.getName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        return toDto(cpuArchitectureRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CpuArchitectureEntity entity = findEntity(id);
        boolean isAdmin = isAdminUser();
        if (Boolean.TRUE.equals(entity.getBuiltIn()) && !isAdmin) {
            throw new IllegalArgumentException("内置 CPU 架构不可删除: " + entity.getName());
        }
        long usageCount = serverRepository.countByCpuArchitecture(entity.getCode());
        if (usageCount > 0) {
            throw new IllegalArgumentException("该架构下仍有 " + usageCount + " 台服务器，无法删除");
        }
        cpuArchitectureRepository.delete(entity);
    }

    private boolean isAdminUser() {
        CurrentUser user = SecurityUtils.getUser();
        if (user == null) return false;
        if (user.getPermissions() != null && user.getPermissions().contains("*:*:*")) return true;
        if (user.getRoles() != null) {
            return user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleCode()));
        }
        return false;
    }

    private CpuArchitectureEntity findEntity(Long id) {
        return cpuArchitectureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CPU 架构不存在: " + id));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("架构名称不能为空");
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

    private CpuArchitectureDTO toDto(CpuArchitectureEntity entity) {
        CpuArchitectureDTO dto = new CpuArchitectureDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}

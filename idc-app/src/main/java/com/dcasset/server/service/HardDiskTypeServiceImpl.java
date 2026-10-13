package com.dcasset.server.service;

import com.dcasset.accessory.repository.HardDiskRepository;
import com.dcasset.common.dto.HardDiskTypeDTO;
import com.dcasset.security.CurrentUser;
import com.dcasset.security.SecurityUtils;
import com.dcasset.server.entity.HardDiskTypeEntity;
import com.dcasset.server.repository.HardDiskTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HardDiskTypeServiceImpl implements HardDiskTypeService {

    private final HardDiskTypeRepository hardDiskTypeRepository;
    private final HardDiskRepository hardDiskRepository;

    @Override
    public List<HardDiskTypeDTO> list() {
        return hardDiskTypeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public HardDiskTypeDTO getById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    public HardDiskTypeDTO getByCode(String code) {
        return hardDiskTypeRepository.findByCode(normalizeCode(code))
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("硬盘类型不存在: " + code));
    }

    @Override
    @Transactional
    public HardDiskTypeDTO create(HardDiskTypeDTO dto) {
        String code = normalizeCode(dto.getCode());
        if (code.isBlank()) {
            throw new IllegalArgumentException("硬盘类型编码不能为空");
        }
        if (hardDiskTypeRepository.existsByCode(code)) {
            throw new IllegalArgumentException("硬盘类型编码已存在: " + code);
        }
        validateName(dto.getName());

        HardDiskTypeEntity entity = new HardDiskTypeEntity();
        entity.setCode(code);
        entity.setName(dto.getName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setBuiltIn(false);
        return toDto(hardDiskTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public HardDiskTypeDTO update(Long id, HardDiskTypeDTO dto) {
        HardDiskTypeEntity entity = findEntity(id);
        validateName(dto.getName());
        entity.setName(dto.getName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        return toDto(hardDiskTypeRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        HardDiskTypeEntity entity = findEntity(id);
        boolean isAdmin = isAdminUser();
        if (Boolean.TRUE.equals(entity.getBuiltIn()) && !isAdmin) {
            throw new IllegalArgumentException("内置硬盘类型不可删除: " + entity.getName());
        }
        long usageCount = hardDiskRepository.countByDiskType(entity.getCode());
        if (usageCount > 0) {
            throw new IllegalArgumentException("该硬盘类型下仍有 " + usageCount + " 块硬盘，无法删除");
        }
        hardDiskTypeRepository.delete(entity);
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

    private HardDiskTypeEntity findEntity(Long id) {
        return hardDiskTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("硬盘类型不存在: " + id));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("硬盘类型名称不能为空");
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

    private HardDiskTypeDTO toDto(HardDiskTypeEntity entity) {
        HardDiskTypeDTO dto = new HardDiskTypeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}


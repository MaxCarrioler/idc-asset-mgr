package com.dcasset.accessory.service;

import com.dcasset.accessory.entity.SparePartCategoryEntity;
import com.dcasset.accessory.repository.SparePartCategoryRepository;
import com.dcasset.accessory.repository.SparePartRepository;
import com.dcasset.common.dto.SparePartCategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SparePartCategoryServiceImpl implements SparePartCategoryService {

    private final SparePartCategoryRepository categoryRepository;
    private final SparePartRepository sparePartRepository;

    @Override
    public List<SparePartCategoryDTO> list() {
        return categoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SparePartCategoryDTO getById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public SparePartCategoryDTO create(SparePartCategoryDTO dto) {
        String code = normalizeCode(dto.getCode());
        if (code.isBlank()) {
            throw new IllegalArgumentException("种类编码不能为空");
        }
        if (categoryRepository.existsByCode(code)) {
            throw new IllegalArgumentException("种类编码已存在: " + code);
        }
        validateName(dto.getName());

        SparePartCategoryEntity entity = new SparePartCategoryEntity();
        entity.setCode(code);
        entity.setName(dto.getName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setBuiltIn(false);
        return toDto(categoryRepository.save(entity));
    }

    @Override
    @Transactional
    public SparePartCategoryDTO update(Long id, SparePartCategoryDTO dto) {
        SparePartCategoryEntity entity = findEntity(id);
        validateName(dto.getName());
        entity.setName(dto.getName().trim());
        entity.setDescription(trimToNull(dto.getDescription()));
        return toDto(categoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SparePartCategoryEntity entity = findEntity(id);
        if (Boolean.TRUE.equals(entity.getBuiltIn())) {
            throw new IllegalArgumentException("内置备件种类不可删除: " + entity.getName());
        }
        long itemCount = sparePartRepository.countByCategoryId(id);
        if (itemCount > 0) {
            throw new IllegalArgumentException("该种类下仍有 " + itemCount + " 条备件记录，无法删除");
        }
        categoryRepository.delete(entity);
    }

    private SparePartCategoryEntity findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("备件种类不存在: " + id));
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("种类名称不能为空");
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

    private SparePartCategoryDTO toDto(SparePartCategoryEntity entity) {
        SparePartCategoryDTO dto = new SparePartCategoryDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}

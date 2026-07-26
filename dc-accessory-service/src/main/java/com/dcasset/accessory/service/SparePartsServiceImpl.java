package com.dcasset.accessory.service;

import com.dcasset.accessory.entity.SparePartCategoryEntity;
import com.dcasset.accessory.entity.SparePartEntity;
import com.dcasset.accessory.repository.SparePartCategoryRepository;
import com.dcasset.accessory.repository.SparePartRepository;
import com.dcasset.common.dto.SparePartDTO;
import com.dcasset.common.dto.SparePartSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SparePartsServiceImpl implements SparePartsService {

    private final SparePartCategoryRepository categoryRepository;
    private final SparePartRepository sparePartRepository;

    @Override
    public List<SparePartSummaryDTO> getSummary() {
        List<SparePartCategoryEntity> categories = categoryRepository.findAll().stream()
                .sorted(Comparator.comparing(SparePartCategoryEntity::getId))
                .collect(Collectors.toList());
        Map<Long, List<SparePartEntity>> itemsByCategory = sparePartRepository.findAll().stream()
                .collect(Collectors.groupingBy(SparePartEntity::getCategoryId));

        return categories.stream()
                .map(category -> {
                    List<SparePartEntity> items = itemsByCategory.getOrDefault(category.getId(), List.of());
                    int count = items.size();
                    return new SparePartSummaryDTO(
                            category.getId(),
                            category.getCode(),
                            category.getName(),
                            count,
                            count
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SparePartDTO> listByCategory(Long categoryId) {
        findCategory(categoryId);
        return sparePartRepository.findByCategoryIdOrderByIdAsc(categoryId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SparePartDTO getById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public SparePartDTO create(SparePartDTO dto) {
        SparePartCategoryEntity category = findCategory(dto.getCategoryId());
        validateItem(dto, null);

        SparePartEntity entity = new SparePartEntity();
        entity.setCategoryId(category.getId());
        applyItemFields(entity, dto);
        return toDto(sparePartRepository.save(entity));
    }

    @Override
    @Transactional
    public SparePartDTO update(Long id, SparePartDTO dto) {
        SparePartEntity entity = findEntity(id);
        findCategory(dto.getCategoryId());
        validateItem(dto, id);

        entity.setCategoryId(dto.getCategoryId());
        applyItemFields(entity, dto);
        return toDto(sparePartRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        sparePartRepository.delete(findEntity(id));
    }

    private SparePartCategoryEntity findCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("备件种类不能为空");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("备件种类不存在: " + categoryId));
    }

    private SparePartEntity findEntity(Long id) {
        return sparePartRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("备件不存在: " + id));
    }

    private void validateItem(SparePartDTO dto, Long excludeId) {
        if (dto.getSerialNumber() == null || dto.getSerialNumber().isBlank()) {
            throw new IllegalArgumentException("序列号不能为空");
        }
        String serialNumber = dto.getSerialNumber().trim();
        sparePartRepository.findByCategoryIdAndSerialNumber(dto.getCategoryId(), serialNumber)
                .ifPresent(existing -> {
                    if (excludeId == null || !existing.getId().equals(excludeId)) {
                        throw new IllegalArgumentException("该种类下序列号已存在: " + serialNumber);
                    }
                });
    }

    private void applyItemFields(SparePartEntity entity, SparePartDTO dto) {
        entity.setManufacturer(trimToNull(dto.getManufacturer()));
        entity.setSerialNumber(dto.getSerialNumber().trim());
        entity.setSpecSummary(trimToNull(dto.getSpecSummary()));
        entity.setLocation(trimToNull(dto.getLocation()));
        entity.setRemark(trimToNull(dto.getRemark()));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private SparePartDTO toDto(SparePartEntity entity) {
        SparePartDTO dto = new SparePartDTO();
        BeanUtils.copyProperties(entity, dto);
        categoryRepository.findById(entity.getCategoryId())
                .ifPresent(category -> dto.setCategoryName(category.getName()));
        return dto;
    }
}

package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.dc.accessory.entity.AccessoryOutboundEntity;
import com.pig4cloud.pig.dc.accessory.entity.SparePartCategoryEntity;
import com.pig4cloud.pig.dc.accessory.entity.SparePartEntity;
import com.pig4cloud.pig.dc.accessory.repository.AccessoryOutboundRepository;
import com.pig4cloud.pig.dc.accessory.repository.SparePartCategoryRepository;
import com.pig4cloud.pig.dc.accessory.repository.SparePartRepository;
import com.pig4cloud.pig.dc.common.dto.SparePartDTO;
import com.pig4cloud.pig.dc.common.dto.SparePartSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SparePartsServiceImpl implements SparePartsService {

    private final SparePartCategoryRepository categoryRepository;
    private final SparePartRepository sparePartRepository;
    private final AccessoryOutboundRepository outboundRepository;

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
                    int inStock = (int) items.stream()
                            .filter(p -> p.getStatus() == null || "IN_STOCK".equals(p.getStatus()))
                            .count();
                    int outCount = (int) items.stream()
                            .filter(p -> "OUT".equals(p.getStatus()))
                            .count();
                    return new SparePartSummaryDTO(
                            category.getId(),
                            category.getCode(),
                            category.getName(),
                            inStock,
                            items.size(),
                            outCount
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SparePartDTO> listByCategory(Long categoryId) {
        findCategory(categoryId);
        List<SparePartEntity> parts = sparePartRepository.findByCategoryIdOrderByIdAsc(categoryId);
        Map<Long, AccessoryOutboundEntity> latestOutbound = latestOutboundBySparePartId(
                parts.stream().map(SparePartEntity::getId).collect(Collectors.toList()));

        return parts.stream()
                .map(part -> {
                    AccessoryOutboundEntity outbound = latestOutbound.get(part.getId());
                    if (outbound != null) {
                        syncActiveServerFromOutbound(part, outbound);
                    }
                    return toDto(part);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SparePartDTO getById(Long id) {
        SparePartEntity entity = findEntity(id);
        outboundRepository.findFirstBySparePartIdOrderByOutboundTimeDesc(id)
                .ifPresent(outbound -> syncActiveServerFromOutbound(entity, outbound));
        return toDto(entity);
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

        if (dto.getSourceServerSerial() == null || dto.getSourceServerSerial().isBlank()) {
            throw new IllegalArgumentException("来源服务器不能为空（可选：无、采购或真实服务器）");
        }
        String sourceSerial = dto.getSourceServerSerial().trim();
        if ("无".equals(sourceSerial) || "采购".equals(sourceSerial)) {
            if (dto.getSourceServerId() != null) {
                throw new IllegalArgumentException("来源为「" + sourceSerial + "」时不能指定服务器 ID");
            }
            return;
        }
        if (dto.getSourceServerId() == null) {
            throw new IllegalArgumentException("自定义来源服务器时必须指定服务器");
        }
    }

    private void applyItemFields(SparePartEntity entity, SparePartDTO dto) {
        entity.setManufacturer(trimToNull(dto.getManufacturer()));
        entity.setSerialNumber(dto.getSerialNumber().trim());
        entity.setSpecSummary(trimToNull(dto.getSpecSummary()));
        entity.setLocation(trimToNull(dto.getLocation()));
        entity.setRemark(trimToNull(dto.getRemark()));
        applySourceServer(entity, dto);
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("IN_STOCK");
        }
    }

    private void applySourceServer(SparePartEntity entity, SparePartDTO dto) {
        String sourceSerial = dto.getSourceServerSerial().trim();
        if ("无".equals(sourceSerial) || "采购".equals(sourceSerial)) {
            entity.setSourceServerId(null);
            entity.setSourceServerSerial(sourceSerial);
            entity.setSourceServerName(null);
            return;
        }
        entity.setSourceServerId(dto.getSourceServerId());
        entity.setSourceServerSerial(sourceSerial);
        entity.setSourceServerName(trimToNull(dto.getSourceServerName()));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 现役服务器以配件中心最新流转（出库）记录为准，保证与流转记录一致。
     */
    private void syncActiveServerFromOutbound(SparePartEntity part, AccessoryOutboundEntity outbound) {
        boolean changed = !Objects.equals(part.getActiveServerId(), outbound.getTargetServerId())
                || !Objects.equals(part.getActiveServerSerial(), outbound.getTargetServerSerial())
                || !Objects.equals(part.getActiveServerName(), outbound.getTargetServerName());
        if (!changed) {
            return;
        }
        part.setActiveServerId(outbound.getTargetServerId());
        part.setActiveServerSerial(outbound.getTargetServerSerial());
        part.setActiveServerName(outbound.getTargetServerName());
        if ("OUT".equals(part.getStatus()) || part.getStatus() == null) {
            part.setStatus("OUT");
        }
        sparePartRepository.save(part);
    }

    private Map<Long, AccessoryOutboundEntity> latestOutboundBySparePartId(List<Long> sparePartIds) {
        if (sparePartIds == null || sparePartIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, AccessoryOutboundEntity> latest = new HashMap<>();
        for (AccessoryOutboundEntity record : outboundRepository.findBySparePartIdInOrderByOutboundTimeDesc(sparePartIds)) {
            latest.putIfAbsent(record.getSparePartId(), record);
        }
        return latest;
    }

    private SparePartDTO toDto(SparePartEntity entity) {
        SparePartDTO dto = new SparePartDTO();
        BeanUtils.copyProperties(entity, dto);
        categoryRepository.findById(entity.getCategoryId())
                .ifPresent(category -> dto.setCategoryName(category.getName()));
        return dto;
    }
}

package com.dcasset.accessory.service;

import com.dcasset.accessory.entity.AccessoryOutboundEntity;
import com.dcasset.accessory.entity.SparePartCategoryEntity;
import com.dcasset.accessory.entity.SparePartEntity;
import com.dcasset.accessory.repository.AccessoryOutboundRepository;
import com.dcasset.accessory.repository.SparePartCategoryRepository;
import com.dcasset.accessory.repository.SparePartRepository;
import com.dcasset.common.dto.SparePartDTO;
import com.dcasset.common.dto.SparePartSummaryDTO;
import com.dcasset.common.dto.SparePartTypeSummaryDTO;
import com.dcasset.security.CurrentUser;
import com.dcasset.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private static final LinkedHashMap<String, String> TYPE_LABELS = new LinkedHashMap<>();
    static {
        TYPE_LABELS.put("cpu", "CPU");
        TYPE_LABELS.put("memory", "内存");
        TYPE_LABELS.put("hard-disk", "硬盘");
        TYPE_LABELS.put("network-card", "网卡");
        TYPE_LABELS.put("raid-sas-card", "RAID/SAS卡");
        TYPE_LABELS.put("hba-card", "HBA卡");
        TYPE_LABELS.put("power-module", "电源模块");
    }

    @Override
    public List<SparePartTypeSummaryDTO> getTypeSummaries() {
        List<SparePartCategoryEntity> allCategories = categoryRepository.findAll();
        Map<Long, List<SparePartEntity>> itemsByCategory = sparePartRepository.findAll().stream()
                .collect(Collectors.groupingBy(SparePartEntity::getCategoryId));

        Map<String, int[]> agg = new LinkedHashMap<>();
        for (String type : TYPE_LABELS.keySet()) {
            agg.put(type, new int[]{0, 0, 0});
        }
        for (SparePartCategoryEntity cat : allCategories) {
            String type = cat.getAccessoryType();
            if (type == null || !agg.containsKey(type)) continue;
            int[] counts = agg.get(type);
            List<SparePartEntity> items = itemsByCategory.getOrDefault(cat.getId(), List.of());
            counts[0] += (int) items.stream().filter(p -> p.getStatus() == null || "IN_STOCK".equals(p.getStatus())).count();
            counts[1] += (int) items.stream().filter(p -> "OUT".equals(p.getStatus())).count();
            counts[2]++;
        }

        List<SparePartTypeSummaryDTO> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : TYPE_LABELS.entrySet()) {
            int[] counts = agg.get(entry.getKey());
            result.add(new SparePartTypeSummaryDTO(entry.getKey(), entry.getValue(), counts[0], counts[1], counts[2]));
        }
        return result;
    }

    @Override
    public List<SparePartSummaryDTO> listByType(String accessoryType) {
        List<SparePartCategoryEntity> categories = categoryRepository.findByAccessoryTypeOrderByIdAsc(accessoryType);
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
    @Transactional(readOnly = true)
    public List<SparePartDTO> listItemsByType(String accessoryType) {
        List<SparePartCategoryEntity> categories = categoryRepository.findByAccessoryTypeOrderByIdAsc(accessoryType);
        if (categories.isEmpty()) {
            return List.of();
        }
        List<Long> categoryIds = categories.stream().map(SparePartCategoryEntity::getId).collect(Collectors.toList());
        Map<Long, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(SparePartCategoryEntity::getId, SparePartCategoryEntity::getName));

        List<SparePartEntity> allParts = sparePartRepository.findAll().stream()
                .filter(p -> categoryIds.contains(p.getCategoryId()))
                .collect(Collectors.toList());

        Map<Long, AccessoryOutboundEntity> latestOutbound = latestOutboundBySparePartId(
                allParts.stream().map(SparePartEntity::getId).collect(Collectors.toList()));

        return allParts.stream()
                .map(part -> {
                    AccessoryOutboundEntity outbound = latestOutbound.get(part.getId());
                    if (outbound != null) {
                        syncActiveServerFromOutbound(part, outbound);
                    }
                    SparePartDTO dto = toDto(part);
                    dto.setCategoryName(categoryNameMap.get(part.getCategoryId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
        SparePartEntity entity = findEntity(id);
        String status = entity.getStatus();
        boolean isAdmin = isAdminUser();
        if ("RESERVED".equals(status)) {
            if (!isAdmin) {
                throw new IllegalArgumentException("备件已被调取申请预留，无法删除：" + entity.getSerialNumber());
            }
        }
        if ("OUT".equals(status)) {
            if (!isAdmin) {
                throw new IllegalArgumentException("备件已出库装机、存在流转记录，无法删除：" + entity.getSerialNumber());
            }
        }
        sparePartRepository.delete(entity);
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
        // 仅用于读取时修正展示口径（现役服务器以最新出库记录为准），不在读路径写库；
        // 出库操作已在 outbound() 中持久化这些字段，无需在 GET 中重复落库。
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

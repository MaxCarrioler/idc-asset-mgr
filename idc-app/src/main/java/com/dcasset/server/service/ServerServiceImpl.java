package com.dcasset.server.service;

import com.dcasset.accessory.entity.AccessoryOutboundEntity;
import com.dcasset.accessory.entity.AccessoryTransferRequestEntity;
import com.dcasset.accessory.entity.SparePartEntity;
import com.dcasset.accessory.repository.AccessoryOutboundRepository;
import com.dcasset.accessory.repository.AccessoryTransferRequestRepository;
import com.dcasset.accessory.repository.CpuRepository;
import com.dcasset.accessory.repository.HardDiskRepository;
import com.dcasset.accessory.repository.HbaCardRepository;
import com.dcasset.accessory.repository.MemoryRepository;
import com.dcasset.accessory.repository.NetworkCardRepository;
import com.dcasset.accessory.repository.PowerModuleRepository;
import com.dcasset.accessory.repository.RaidSasCardRepository;
import com.dcasset.accessory.repository.SparePartRepository;
import com.dcasset.common.dto.ServerDTO;
import com.dcasset.common.dto.ServerOperationRecordDTO;
import com.dcasset.common.result.PageResult;
import com.dcasset.server.entity.ServerEntity;
import com.dcasset.server.entity.ServerOperationRecordEntity;
import com.dcasset.server.repository.ServerOperationRecordRepository;
import com.dcasset.server.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private static final Map<String, String> FIELD_LABELS = new LinkedHashMap<>();

    static {
        FIELD_LABELS.put("serverName", "服务器名称");
        FIELD_LABELS.put("ipAddress", "IP地址");
        FIELD_LABELS.put("rackLocation", "机架位置");
        FIELD_LABELS.put("region", "Region名称");
        FIELD_LABELS.put("serverType", "服务器类型");
        FIELD_LABELS.put("serverModel", "型号");
        FIELD_LABELS.put("manufacturer", "厂商");
        FIELD_LABELS.put("serialNumber", "序列号");
        FIELD_LABELS.put("status", "状态");
        FIELD_LABELS.put("osType", "操作系统");
        FIELD_LABELS.put("cpuArchitecture", "CPU架构");
        FIELD_LABELS.put("remark", "备注");
    }

    private final ServerRepository serverRepository;
    private final ServerOperationRecordRepository operationRecordRepository;
    private final ServerTypeService serverTypeService;
    private final CpuArchitectureService cpuArchitectureService;
    private final CpuRepository cpuRepository;
    private final MemoryRepository memoryRepository;
    private final HardDiskRepository hardDiskRepository;
    private final NetworkCardRepository networkCardRepository;
    private final HbaCardRepository hbaCardRepository;
    private final RaidSasCardRepository raidSasCardRepository;
    private final PowerModuleRepository powerModuleRepository;
    private final AccessoryTransferRequestRepository transferRequestRepository;
    private final AccessoryOutboundRepository outboundRepository;
    private final SparePartRepository sparePartRepository;

    @Override
    public PageResult<ServerDTO> list(String keyword, int page, int size) {
        Page<ServerEntity> pageData = serverRepository.search(keyword, PageRequest.of(page - 1, size));
        List<ServerDTO> records = pageData.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageResult<>(records, pageData.getTotalElements(), page, size, pageData.getTotalPages());
    }

    @Override
    public List<ServerDTO> listAll() {
        return serverRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ServerDTO getById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public ServerDTO create(ServerDTO dto) {
        validateServerType(dto.getServerType());
        validateCpuArchitecture(dto.getCpuArchitecture());
        ServerEntity entity = new ServerEntity();
        BeanUtils.copyProperties(dto, entity, "id", "updatedAt", "changeReason");
        entity.setId((Long) null);
        entity.setServerType(normalizeServerType(dto.getServerType()));
        ServerEntity saved = serverRepository.save(entity);
        saveOperationRecord(saved.getId(), "新建服务器：" + saved.getServerName(), "初始录入");
        return toDto(saved);
    }

    @Override
    @Transactional
    public ServerDTO update(Long id, ServerDTO dto) {
        validateServerType(dto.getServerType());
        validateCpuArchitecture(dto.getCpuArchitecture());
        ServerEntity entity = findEntity(id);
        ServerDTO oldDto = toDto(entity);
        BeanUtils.copyProperties(dto, entity, "id", "createdAt", "changeReason");
        entity.setServerType(normalizeServerType(dto.getServerType()));
        ServerEntity saved = serverRepository.save(entity);

        String changeContent = buildChangeContent(oldDto, dto);
        if (!changeContent.isEmpty()) {
            saveOperationRecord(id, changeContent, dto.getChangeReason());
        }

        // 级联更新关联记录中的服务器名称
        if (!Objects.equals(oldDto.getServerName(), dto.getServerName())) {
            cascadeUpdateServerName(id, dto.getServerName());
        }

        return toDto(saved);
    }

    private void cascadeUpdateServerName(Long serverId, String newName) {
        if (newName == null) return;
        // 更新调取申请中的目标服务器名称
        List<AccessoryTransferRequestEntity> requests = transferRequestRepository.findByTargetServerId(serverId);
        for (AccessoryTransferRequestEntity req : requests) {
            req.setTargetServerName(newName);
            transferRequestRepository.save(req);
        }
        // 更新出库记录中的来源/目标服务器名称
        List<AccessoryOutboundEntity> asSource = outboundRepository.findBySourceServerId(serverId);
        for (AccessoryOutboundEntity ob : asSource) {
            ob.setSourceServerName(newName);
            outboundRepository.save(ob);
        }
        List<AccessoryOutboundEntity> asTarget = outboundRepository.findByTargetServerId(serverId);
        for (AccessoryOutboundEntity ob : asTarget) {
            ob.setTargetServerName(newName);
            outboundRepository.save(ob);
        }
        // 更新备件中的来源/现役服务器名称
        List<SparePartEntity> spareAsSource = sparePartRepository.findBySourceServerId(serverId);
        for (SparePartEntity part : spareAsSource) {
            part.setSourceServerName(newName);
            sparePartRepository.save(part);
        }
        List<SparePartEntity> asActive = sparePartRepository.findByActiveServerId(serverId);
        for (SparePartEntity part : asActive) {
            part.setActiveServerName(newName);
            sparePartRepository.save(part);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ServerEntity entity = findEntity(id);
        // 级联清理该服务器下的全部配件，避免遗留孤儿数据（各配件仓库已提供 deleteByServerId）
        cpuRepository.deleteByServerId(id);
        memoryRepository.deleteByServerId(id);
        hardDiskRepository.deleteByServerId(id);
        networkCardRepository.deleteByServerId(id);
        hbaCardRepository.deleteByServerId(id);
        raidSasCardRepository.deleteByServerId(id);
        powerModuleRepository.deleteByServerId(id);
        serverRepository.delete(entity);
        operationRecordRepository.findByServerIdOrderByModifiedAtDesc(id)
                .forEach(record -> operationRecordRepository.delete(record));
    }

    @Override
    public List<ServerOperationRecordDTO> listOperationRecords(Long serverId) {
        findEntity(serverId);
        return operationRecordRepository.findByServerIdOrderByModifiedAtDesc(serverId).stream()
                .map(this::toRecordDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listRegions() {
        return serverRepository.findDistinctRegions();
    }

    @Override
    public List<String> listCpuArchitectures() {
        return cpuArchitectureService.list().stream()
                .map(dto -> dto.getCode())
                .toList();
    }

    private void validateCpuArchitecture(String cpuArchitecture) {
        if (cpuArchitecture == null || cpuArchitecture.isBlank()) {
            return;
        }
        cpuArchitectureService.getByCode(cpuArchitecture);
    }

    private void saveOperationRecord(Long serverId, String changeContent, String changeReason) {
        ServerOperationRecordEntity record = new ServerOperationRecordEntity();
        record.setServerId(serverId);
        record.setChangeContent(changeContent);
        record.setChangeReason(changeReason != null && !changeReason.isBlank() ? changeReason.trim() : "未填写");
        operationRecordRepository.save(record);
    }

    private String buildChangeContent(ServerDTO oldDto, ServerDTO newDto) {
        StringBuilder sb = new StringBuilder();
        compareField(sb, "serverName", oldDto.getServerName(), newDto.getServerName());
        compareField(sb, "ipAddress", oldDto.getIpAddress(), newDto.getIpAddress());
        compareField(sb, "rackLocation", oldDto.getRackLocation(), newDto.getRackLocation());
        compareField(sb, "region", oldDto.getRegion(), newDto.getRegion());
        compareField(sb, "serverType", oldDto.getServerType(), newDto.getServerType());
        compareField(sb, "serverModel", oldDto.getServerModel(), newDto.getServerModel());
        compareField(sb, "manufacturer", oldDto.getManufacturer(), newDto.getManufacturer());
        compareField(sb, "serialNumber", oldDto.getSerialNumber(), newDto.getSerialNumber());
        compareField(sb, "status", oldDto.getStatus(), newDto.getStatus());
        compareField(sb, "osType", oldDto.getOsType(), newDto.getOsType());
        compareField(sb, "cpuArchitecture", oldDto.getCpuArchitecture(), newDto.getCpuArchitecture());
        compareField(sb, "remark", oldDto.getRemark(), newDto.getRemark());
        return sb.toString().trim();
    }

    private void compareField(StringBuilder sb, String field, String oldVal, String newVal) {
        String oldStr = oldVal == null ? "" : oldVal;
        String newStr = newVal == null ? "" : newVal;
        if (Objects.equals(oldStr, newStr)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append("；");
        }
        sb.append(FIELD_LABELS.get(field))
                .append("：")
                .append(displayValue(oldStr))
                .append(" → ")
                .append(displayValue(newStr));
    }

    private String displayValue(String value) {
        return value.isEmpty() ? "空" : value;
    }

    private void validateServerType(String serverType) {
        if (serverType == null || serverType.isBlank()) {
            throw new IllegalArgumentException("服务器类型不能为空");
        }
        serverTypeService.getByCode(serverType);
    }

    private String normalizeServerType(String serverType) {
        return serverType == null ? null : serverType.trim().toUpperCase();
    }

    private ServerEntity findEntity(Long id) {
        return serverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("服务器不存在: " + id));
    }

    private ServerDTO toDto(ServerEntity entity) {
        ServerDTO dto = new ServerDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private ServerOperationRecordDTO toRecordDto(ServerOperationRecordEntity entity) {
        ServerOperationRecordDTO dto = new ServerOperationRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}

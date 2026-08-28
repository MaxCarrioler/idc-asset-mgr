package com.dcasset.accessory.service;

import com.dcasset.accessory.entity.CpuEntity;
import com.dcasset.accessory.entity.HardDiskEntity;
import com.dcasset.accessory.entity.MemoryEntity;
import com.dcasset.accessory.repository.CpuRepository;
import com.dcasset.accessory.repository.HardDiskRepository;
import com.dcasset.accessory.repository.MemoryRepository;
import com.dcasset.common.dto.HardwareStatisticsDTO;
import com.dcasset.common.dto.ServerDTO;
import com.dcasset.common.dto.ServerTypeDTO;
import com.dcasset.common.result.PageResult;
import com.dcasset.server.service.ServerService;
import com.dcasset.server.service.ServerTypeService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HardwareStatisticsServiceImpl implements HardwareStatisticsService {

    private static final List<String> DISK_SPECS = List.of("1.8T", "2.4T", "3.84T", "4T", "8T");
    private static final Map<String, Integer> SPEC_CAPACITY_GB = Map.of(
            "1.8T", 1843,
            "2.4T", 2458,
            "3.84T", 3840,
            "4T", 4096,
            "8T", 8192
    );

    private final ServerService serverService;
    private final ServerTypeService serverTypeService;
    private final CpuRepository cpuRepository;
    private final MemoryRepository memoryRepository;
    private final HardDiskRepository hardDiskRepository;

    public HardwareStatisticsServiceImpl(
            ServerService serverService,
            ServerTypeService serverTypeService,
            CpuRepository cpuRepository,
            MemoryRepository memoryRepository,
            HardDiskRepository hardDiskRepository) {
        this.serverService = serverService;
        this.serverTypeService = serverTypeService;
        this.cpuRepository = cpuRepository;
        this.memoryRepository = memoryRepository;
        this.hardDiskRepository = hardDiskRepository;
    }

    @Override
    public HardwareStatisticsDTO getHardwareStatistics(
            List<String> regions,
            List<Long> serverTypeIds,
            List<String> cpuArchitectures) {
        Map<Long, ServerDTO> serverMap = fetchAllServers();
        Map<Long, String> typeIdToCode = fetchServerTypes().stream()
                .collect(Collectors.toMap(ServerTypeDTO::getId, ServerTypeDTO::getCode, (a, b) -> a));

        Set<String> regionFilter = normalizeStringSet(regions);
        Set<Long> typeIdFilter = normalizeLongSet(serverTypeIds);
        Set<String> typeCodeFilter = typeIdFilter.isEmpty()
                ? Set.of()
                : typeIdFilter.stream()
                .map(typeIdToCode::get)
                .filter(code -> code != null && !code.isBlank())
                .map(this::normalizeCode)
                .collect(Collectors.toSet());
        Set<String> cpuArchFilter = normalizeCpuArchitectures(cpuArchitectures);

        Set<Long> matchedServerIds = serverMap.values().stream()
                .filter(server -> matchesRegion(server, regionFilter))
                .filter(server -> matchesServerType(server, typeCodeFilter))
                .filter(server -> matchesCpuArchitecture(server, cpuArchFilter))
                .map(ServerDTO::getId)
                .collect(Collectors.toSet());

        HardwareStatisticsDTO statistics = new HardwareStatisticsDTO();
        statistics.setServerCount(matchedServerIds.size());

        Map<String, DiskSpecAccumulator> specMap = initSpecMap();
        aggregateCpuForServers(cpuRepository.findAll(), matchedServerIds, statistics);
        aggregateMemoryForServers(memoryRepository.findAll(), matchedServerIds, statistics);
        long totalDiskGb = aggregateDisksForServers(hardDiskRepository.findAll(), matchedServerIds, specMap);
        statistics.setTotalDiskCapacityTb(gbToTb(totalDiskGb));

        List<HardwareStatisticsDTO.DiskSpecSummary> diskSpecSummaries = DISK_SPECS.stream()
                .map(spec -> {
                    DiskSpecAccumulator acc = specMap.get(spec);
                    return new HardwareStatisticsDTO.DiskSpecSummary(
                            spec, acc.count, gbToTb(acc.totalGb));
                })
                .collect(Collectors.toList());
        statistics.setDiskSpecSummaries(diskSpecSummaries);

        return statistics;
    }

    @Override
    public Map<Long, ServerDTO> fetchAllServers() {
        // 同进程直调，替代原跨服务 RestTemplate 调用
        PageResult<ServerDTO> page = serverService.list(null, 1, 1000);
        if (page == null || page.getRecords() == null) {
            return Map.of();
        }
        return page.getRecords().stream()
                .collect(Collectors.toMap(ServerDTO::getId, s -> s, (a, b) -> a));
    }

    private List<ServerTypeDTO> fetchServerTypes() {
        return serverTypeService.list();
    }

    private boolean matchesRegion(ServerDTO server, Set<String> regionFilter) {
        if (regionFilter.isEmpty()) {
            return true;
        }
        return server.getRegion() != null && regionFilter.contains(server.getRegion().trim());
    }

    private boolean matchesServerType(ServerDTO server, Set<String> typeCodeFilter) {
        if (typeCodeFilter.isEmpty()) {
            return true;
        }
        return typeCodeFilter.contains(normalizeCode(server.getServerType()));
    }

    private boolean matchesCpuArchitecture(ServerDTO server, Set<String> cpuArchFilter) {
        if (cpuArchFilter.isEmpty()) {
            return true;
        }
        if (server.getCpuArchitecture() == null || server.getCpuArchitecture().isBlank()) {
            return false;
        }
        return cpuArchFilter.contains(server.getCpuArchitecture().trim().toUpperCase());
    }

    private Set<String> normalizeStringSet(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        Set<String> normalized = new HashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                normalized.add(value.trim());
            }
        }
        return normalized;
    }

    private Set<Long> normalizeLongSet(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        return values.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
    }

    private Set<String> normalizeCpuArchitectures(List<String> values) {
        Set<String> normalized = new HashSet<>();
        if (values == null) {
            return normalized;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                normalized.add(value.trim().toUpperCase());
            }
        }
        return normalized;
    }

    private void aggregateCpuForServers(List<CpuEntity> cpus, Set<Long> serverIds,
                                        HardwareStatisticsDTO statistics) {
        for (CpuEntity cpu : cpus) {
            if (!serverIds.contains(cpu.getServerId())) {
                continue;
            }
            int quantity = cpu.getQuantity() != null && cpu.getQuantity() > 0 ? cpu.getQuantity() : 1;
            statistics.setTotalCores(statistics.getTotalCores() + cpu.getCores() * quantity);
            statistics.setTotalThreads(statistics.getTotalThreads() + cpu.getThreads() * quantity);
        }
    }

    private void aggregateMemoryForServers(List<MemoryEntity> memories, Set<Long> serverIds,
                                           HardwareStatisticsDTO statistics) {
        for (MemoryEntity memory : memories) {
            if (!serverIds.contains(memory.getServerId())) {
                continue;
            }
            statistics.setTotalMemoryGb(statistics.getTotalMemoryGb()
                    + (long) memory.getStickCount() * memory.getCapacityPerStickGb());
        }
    }

    private long aggregateDisksForServers(List<HardDiskEntity> disks, Set<Long> serverIds,
                                          Map<String, DiskSpecAccumulator> specMap) {
        long totalGb = 0;
        for (HardDiskEntity disk : disks) {
            if (!serverIds.contains(disk.getServerId())) {
                continue;
            }
            totalGb += disk.getCapacityGb();
            String spec = resolveDiskSpec(disk.getCapacityGb());
            if (spec != null && specMap.containsKey(spec)) {
                DiskSpecAccumulator acc = specMap.get(spec);
                acc.count++;
                acc.totalGb += disk.getCapacityGb();
            }
        }
        return totalGb;
    }

    private Map<String, DiskSpecAccumulator> initSpecMap() {
        Map<String, DiskSpecAccumulator> map = new LinkedHashMap<>();
        for (String spec : DISK_SPECS) {
            map.put(spec, new DiskSpecAccumulator());
        }
        return map;
    }

    private String resolveDiskSpec(int capacityGb) {
        String matchedSpec = null;
        double minDiff = Double.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : SPEC_CAPACITY_GB.entrySet()) {
            double diff = Math.abs(capacityGb - entry.getValue()) / (double) entry.getValue();
            if (diff <= 0.08 && diff < minDiff) {
                minDiff = diff;
                matchedSpec = entry.getKey();
            }
        }
        return matchedSpec;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            return "COMPUTE";
        }
        return code.trim().toUpperCase();
    }

    private double gbToTb(long capacityGb) {
        return Math.round(capacityGb / 1024.0 * 100.0) / 100.0;
    }

    private static class DiskSpecAccumulator {
        private int count;
        private long totalGb;
    }
}

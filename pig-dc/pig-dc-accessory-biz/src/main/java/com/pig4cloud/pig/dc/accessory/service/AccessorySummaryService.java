package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.dc.common.dto.ServerAccessorySummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessorySummaryService {

    private final CpuService cpuService;
    private final MemoryService memoryService;
    private final HardDiskService hardDiskService;
    private final NetworkCardService networkCardService;
    private final HbaCardService hbaCardService;
    private final RaidSasCardService raidSasCardService;
    private final PowerModuleService powerModuleService;

    public ServerAccessorySummaryDTO getSummaryByServerId(Long serverId) {
        ServerAccessorySummaryDTO summary = new ServerAccessorySummaryDTO();
        summary.setServerId(serverId);
        summary.setCpus(cpuService.listByServerId(serverId));
        summary.setMemories(memoryService.listByServerId(serverId));
        summary.setHardDisks(hardDiskService.listByServerId(serverId));
        summary.setNetworkCards(networkCardService.listByServerId(serverId));
        summary.setHbaCards(hbaCardService.listByServerId(serverId));
        summary.setRaidSasCards(raidSasCardService.listByServerId(serverId));
        summary.setPowerModules(powerModuleService.listByServerId(serverId));
        return summary;
    }
}

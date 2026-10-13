package com.dcasset.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class ServerAccessorySummaryDTO {

    private Long serverId;
    private List<CpuDTO> cpus;
    private List<MemoryDTO> memories;
    private List<HardDiskDTO> hardDisks;
    private List<NetworkCardDTO> networkCards;
    private List<HbaCardDTO> hbaCards;
    private List<RaidSasCardDTO> raidSasCards;
    private List<PowerModuleDTO> powerModules;
}

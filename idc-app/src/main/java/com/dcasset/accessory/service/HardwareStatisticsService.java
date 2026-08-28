package com.dcasset.accessory.service;

import com.dcasset.common.dto.HardwareStatisticsDTO;
import com.dcasset.common.dto.ServerDTO;

import java.util.List;
import java.util.Map;

public interface HardwareStatisticsService {

    HardwareStatisticsDTO getHardwareStatistics(
            List<String> regions,
            List<Long> serverTypeIds,
            List<String> cpuArchitectures);

    Map<Long, ServerDTO> fetchAllServers();
}

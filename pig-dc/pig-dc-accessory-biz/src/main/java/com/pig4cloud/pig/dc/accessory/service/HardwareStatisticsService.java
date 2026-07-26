package com.pig4cloud.pig.dc.accessory.service;

import com.pig4cloud.pig.dc.common.dto.HardwareStatisticsDTO;
import com.pig4cloud.pig.dc.common.dto.ServerDTO;

import java.util.List;
import java.util.Map;

public interface HardwareStatisticsService {

    HardwareStatisticsDTO getHardwareStatistics(
            List<String> regions,
            List<Long> serverTypeIds,
            List<String> cpuArchitectures);

    Map<Long, ServerDTO> fetchAllServers();
}

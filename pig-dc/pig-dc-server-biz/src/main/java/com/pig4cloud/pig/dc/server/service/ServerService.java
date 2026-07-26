package com.pig4cloud.pig.dc.server.service;

import com.pig4cloud.pig.dc.common.dto.ServerDTO;
import com.pig4cloud.pig.dc.common.dto.ServerOperationRecordDTO;
import com.pig4cloud.pig.dc.common.result.PageResult;

import java.util.List;

public interface ServerService {

    PageResult<ServerDTO> list(String keyword, int page, int size);

    ServerDTO getById(Long id);

    ServerDTO create(ServerDTO dto);

    ServerDTO update(Long id, ServerDTO dto);

    void delete(Long id);

    List<ServerOperationRecordDTO> listOperationRecords(Long serverId);

    List<String> listRegions();

    List<String> listCpuArchitectures();
}

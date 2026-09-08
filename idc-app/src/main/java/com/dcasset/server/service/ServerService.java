package com.dcasset.server.service;

import com.dcasset.common.dto.ServerDTO;
import com.dcasset.common.dto.ServerOperationRecordDTO;
import com.dcasset.common.result.PageResult;

import java.util.List;

public interface ServerService {

    PageResult<ServerDTO> list(String keyword, int page, int size);

    /** 全量服务器（用于统计聚合，避免分页上限导致漏算） */
    List<ServerDTO> listAll();

    ServerDTO getById(Long id);

    ServerDTO create(ServerDTO dto);

    ServerDTO update(Long id, ServerDTO dto);

    void delete(Long id);

    List<ServerOperationRecordDTO> listOperationRecords(Long serverId);

    List<String> listRegions();

    List<String> listCpuArchitectures();
}

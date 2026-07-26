package com.pig4cloud.pig.dc.server.service;

import com.pig4cloud.pig.dc.common.dto.ServerTypeDTO;

import java.util.List;

public interface ServerTypeService {

    List<ServerTypeDTO> list();

    ServerTypeDTO getById(Long id);

    ServerTypeDTO getByCode(String code);

    ServerTypeDTO create(ServerTypeDTO dto);

    ServerTypeDTO update(Long id, ServerTypeDTO dto);

    void delete(Long id);
}

package com.dcasset.server.service;

import com.dcasset.common.dto.CpuArchitectureDTO;

import java.util.List;

public interface CpuArchitectureService {

    List<CpuArchitectureDTO> list();

    CpuArchitectureDTO getById(Long id);

    CpuArchitectureDTO getByCode(String code);

    CpuArchitectureDTO create(CpuArchitectureDTO dto);

    CpuArchitectureDTO update(Long id, CpuArchitectureDTO dto);

    void delete(Long id);
}

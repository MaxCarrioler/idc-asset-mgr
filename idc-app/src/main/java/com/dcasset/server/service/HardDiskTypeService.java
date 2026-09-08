package com.dcasset.server.service;

import com.dcasset.common.dto.HardDiskTypeDTO;

import java.util.List;

public interface HardDiskTypeService {

    List<HardDiskTypeDTO> list();

    HardDiskTypeDTO getById(Long id);

    HardDiskTypeDTO getByCode(String code);

    HardDiskTypeDTO create(HardDiskTypeDTO dto);

    HardDiskTypeDTO update(Long id, HardDiskTypeDTO dto);

    void delete(Long id);
}

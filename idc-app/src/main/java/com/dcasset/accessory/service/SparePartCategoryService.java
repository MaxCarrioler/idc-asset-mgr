package com.dcasset.accessory.service;

import com.dcasset.common.dto.SparePartCategoryDTO;

import java.util.List;

public interface SparePartCategoryService {

    List<SparePartCategoryDTO> list();

    SparePartCategoryDTO getById(Long id);

    SparePartCategoryDTO create(SparePartCategoryDTO dto);

    SparePartCategoryDTO update(Long id, SparePartCategoryDTO dto);

    void delete(Long id);
}

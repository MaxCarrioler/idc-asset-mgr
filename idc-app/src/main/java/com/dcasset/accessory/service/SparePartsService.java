package com.dcasset.accessory.service;

import com.dcasset.common.dto.SparePartDTO;
import com.dcasset.common.dto.SparePartSummaryDTO;
import com.dcasset.common.dto.SparePartTypeSummaryDTO;

import java.util.List;

public interface SparePartsService {

    List<SparePartSummaryDTO> getSummary();

    List<SparePartTypeSummaryDTO> getTypeSummaries();

    List<SparePartSummaryDTO> listByType(String accessoryType);

    List<SparePartDTO> listItemsByType(String accessoryType);

    List<SparePartDTO> listByCategory(Long categoryId);

    SparePartDTO getById(Long id);

    SparePartDTO create(SparePartDTO dto);

    SparePartDTO update(Long id, SparePartDTO dto);

    void delete(Long id);
}

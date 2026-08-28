package com.dcasset.accessory.service;

import com.dcasset.common.dto.*;

import java.util.List;

public interface AccessoryTransferService {

    AccessoryTransferRequestDTO create(AccessoryTransferRequestDTO dto);

    List<AccessoryTransferRequestDTO> list(String status);

    AccessoryTransferRequestDTO getById(Long id);

    AccessoryTransferRequestDTO approve(Long id, AccessoryTransferApproveRequest request);

    AccessoryTransferRequestDTO reject(Long id, String reason);

    List<AccessoryOutboundDTO> outbound(Long id, AccessoryOutboundRequest request);

    List<AccessoryOutboundDTO> listTransfers();

    List<SparePartDTO> listAvailableSpareParts(Long categoryId);
}

package com.dcasset.accessory.service;

import com.dcasset.common.dto.*;

import java.util.List;

public interface AccessoryTransferService {

    AccessoryTransferRequestDTO create(AccessoryTransferRequestDTO dto);

    List<AccessoryTransferRequestDTO> list(String status);

    AccessoryTransferRequestDTO getById(Long id);

    AccessoryTransferRequestDTO approve(Long id, AccessoryTransferApproveRequest request);

    AccessoryTransferRequestDTO reject(Long id, String reason);

    /** 撤销审批：释放已预留备件，申请回退为待审批 */
    AccessoryTransferRequestDTO revoke(Long id);

    List<AccessoryOutboundDTO> outbound(Long id, AccessoryOutboundRequest request);

    List<AccessoryOutboundDTO> listTransfers();

    List<SparePartDTO> listAvailableSpareParts(Long categoryId);

    void deleteRequest(Long id);
}

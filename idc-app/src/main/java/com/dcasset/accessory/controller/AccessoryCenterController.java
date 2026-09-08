package com.dcasset.accessory.controller;

import com.dcasset.accessory.service.AccessoryTransferService;
import com.dcasset.common.dto.*;
import com.dcasset.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "配件中心")
@RestController
@RequestMapping("/api/accessory-center")
@RequiredArgsConstructor
public class AccessoryCenterController {

    private final AccessoryTransferService accessoryTransferService;

    @Operation(summary = "提交调取申请")
    @PreAuthorize("hasAuthority('accessory:transfer:apply')")
    @PostMapping("/requests")
    public Result<AccessoryTransferRequestDTO> create(@Valid @RequestBody AccessoryTransferRequestDTO dto) {
        return Result.success(accessoryTransferService.create(dto));
    }

    @Operation(summary = "申请列表")
    @PreAuthorize("hasAuthority('accessory:transfer:view')")
    @GetMapping("/requests")
    public Result<List<AccessoryTransferRequestDTO>> list(@RequestParam(required = false) String status) {
        return Result.success(accessoryTransferService.list(status));
    }

    @Operation(summary = "申请详情")
    @PreAuthorize("hasAuthority('accessory:transfer:view')")
    @GetMapping("/requests/{id}")
    public Result<AccessoryTransferRequestDTO> getById(@PathVariable Long id) {
        return Result.success(accessoryTransferService.getById(id));
    }

    @Operation(summary = "审批通过（绑定具体序列号）")
    @PreAuthorize("hasAuthority('accessory:transfer:approve')")
    @PostMapping("/requests/{id}/approve")
    public Result<AccessoryTransferRequestDTO> approve(@PathVariable Long id,
                                                       @Valid @RequestBody AccessoryTransferApproveRequest request) {
        return Result.success(accessoryTransferService.approve(id, request));
    }

    @Operation(summary = "审批驳回")
    @PreAuthorize("hasAuthority('accessory:transfer:reject')")
    @PostMapping("/requests/{id}/reject")
    public Result<AccessoryTransferRequestDTO> reject(@PathVariable Long id,
                                                      @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return Result.success(accessoryTransferService.reject(id, reason));
    }

    @Operation(summary = "撤销审批（释放预留备件，回退待审批）")
    @PreAuthorize("hasAuthority('accessory:transfer:approve')")
    @PostMapping("/requests/{id}/revoke")
    public Result<AccessoryTransferRequestDTO> revoke(@PathVariable Long id) {
        return Result.success(accessoryTransferService.revoke(id));
    }

    @Operation(summary = "出库（强制携带来源服务器）")
    @PreAuthorize("hasAuthority('accessory:transfer:outbound')")
    @PostMapping("/requests/{id}/outbound")
    public Result<List<AccessoryOutboundDTO>> outbound(@PathVariable Long id,
                                                       @RequestBody(required = false) AccessoryOutboundRequest request) {
        return Result.success(accessoryTransferService.outbound(id, request == null ? new AccessoryOutboundRequest() : request));
    }

    @Operation(summary = "流转/出库记录")
    @PreAuthorize("hasAuthority('accessory:transfer:view')")
    @GetMapping("/transfers")
    public Result<List<AccessoryOutboundDTO>> transfers() {
        return Result.success(accessoryTransferService.listTransfers());
    }

    @Operation(summary = "可调取的在库备件（按种类）")
    @PreAuthorize("hasAuthority('accessory:transfer:view')")
    @GetMapping("/available-spare-parts")
    public Result<List<SparePartDTO>> availableSpareParts(@RequestParam Long categoryId) {
        return Result.success(accessoryTransferService.listAvailableSpareParts(categoryId));
    }
}

package com.dcasset.accessory.controller;

import com.dcasset.common.dto.HardDiskDTO;
import com.dcasset.accessory.service.BaseAccessoryService;
import com.dcasset.accessory.service.HardDiskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "硬盘配件管理")
@RestController
@RequestMapping("/api/accessories/hard-disks")
@RequiredArgsConstructor
public class HardDiskController extends BaseAccessoryController<HardDiskDTO> {

    private final HardDiskService hardDiskService;

    @Override
    protected BaseAccessoryService<?, HardDiskDTO> getService() {
        return hardDiskService;
    }

    @Override
    protected String getTypeName() {
        return "硬盘";
    }
}

package com.dcasset.accessory.controller;

import com.dcasset.common.dto.MemoryDTO;
import com.dcasset.accessory.service.BaseAccessoryService;
import com.dcasset.accessory.service.MemoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "内存配件管理")
@RestController
@RequestMapping("/api/accessories/memories")
@RequiredArgsConstructor
public class MemoryController extends BaseAccessoryController<MemoryDTO> {

    private final MemoryService memoryService;

    @Override
    protected BaseAccessoryService<?, MemoryDTO> getService() {
        return memoryService;
    }

    @Override
    protected String getTypeName() {
        return "内存";
    }
}

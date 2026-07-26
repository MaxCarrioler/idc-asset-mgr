package com.pig4cloud.pig.dc.accessory.controller;

import com.pig4cloud.pig.dc.common.dto.MemoryDTO;
import com.pig4cloud.pig.dc.accessory.service.BaseAccessoryService;
import com.pig4cloud.pig.dc.accessory.service.MemoryService;
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

package com.dcasset.accessory.controller;

import com.dcasset.common.dto.CpuDTO;
import com.dcasset.accessory.service.BaseAccessoryService;
import com.dcasset.accessory.service.CpuService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "CPU配件管理")
@RestController
@RequestMapping("/api/accessories/cpus")
@RequiredArgsConstructor
public class CpuController extends BaseAccessoryController<CpuDTO> {

    private final CpuService cpuService;

    @Override
    protected BaseAccessoryService<?, CpuDTO> getService() {
        return cpuService;
    }

    @Override
    protected String getTypeName() {
        return "CPU";
    }
}

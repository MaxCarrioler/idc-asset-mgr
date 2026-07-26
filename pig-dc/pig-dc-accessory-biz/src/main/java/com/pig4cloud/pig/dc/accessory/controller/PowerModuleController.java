package com.pig4cloud.pig.dc.accessory.controller;

import com.pig4cloud.pig.dc.common.dto.PowerModuleDTO;
import com.pig4cloud.pig.dc.accessory.service.BaseAccessoryService;
import com.pig4cloud.pig.dc.accessory.service.PowerModuleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "电源模块配件管理")
@RestController
@RequestMapping("/api/accessories/power-modules")
@RequiredArgsConstructor
public class PowerModuleController extends BaseAccessoryController<PowerModuleDTO> {

    private final PowerModuleService powerModuleService;

    @Override
    protected BaseAccessoryService<?, PowerModuleDTO> getService() {
        return powerModuleService;
    }

    @Override
    protected String getTypeName() {
        return "电源模块";
    }
}

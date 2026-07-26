package com.pig4cloud.pig.dc.accessory.controller;

import com.pig4cloud.pig.dc.common.dto.HbaCardDTO;
import com.pig4cloud.pig.dc.accessory.service.BaseAccessoryService;
import com.pig4cloud.pig.dc.accessory.service.HbaCardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "HBA卡配件管理")
@RestController
@RequestMapping("/api/accessories/hba-cards")
@RequiredArgsConstructor
public class HbaCardController extends BaseAccessoryController<HbaCardDTO> {

    private final HbaCardService hbaCardService;

    @Override
    protected BaseAccessoryService<?, HbaCardDTO> getService() {
        return hbaCardService;
    }

    @Override
    protected String getTypeName() {
        return "HBA卡";
    }
}

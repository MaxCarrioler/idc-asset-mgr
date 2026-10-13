package com.dcasset.accessory.controller;

import com.dcasset.common.dto.RaidSasCardDTO;
import com.dcasset.accessory.service.BaseAccessoryService;
import com.dcasset.accessory.service.RaidSasCardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RAID/SAS卡配件管理")
@RestController
@RequestMapping("/api/accessories/raid-sas-cards")
@RequiredArgsConstructor
public class RaidSasCardController extends BaseAccessoryController<RaidSasCardDTO> {

    private final RaidSasCardService raidSasCardService;

    @Override
    protected BaseAccessoryService<?, RaidSasCardDTO> getService() {
        return raidSasCardService;
    }

    @Override
    protected String getTypeName() {
        return "RAID/SAS卡";
    }
}

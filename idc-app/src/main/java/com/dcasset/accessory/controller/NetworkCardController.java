package com.dcasset.accessory.controller;

import com.dcasset.common.dto.NetworkCardDTO;
import com.dcasset.accessory.service.BaseAccessoryService;
import com.dcasset.accessory.service.NetworkCardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "网卡配件管理")
@RestController
@RequestMapping("/api/accessories/network-cards")
@RequiredArgsConstructor
public class NetworkCardController extends BaseAccessoryController<NetworkCardDTO> {

    private final NetworkCardService networkCardService;

    @Override
    protected BaseAccessoryService<?, NetworkCardDTO> getService() {
        return networkCardService;
    }

    @Override
    protected String getTypeName() {
        return "网卡";
    }
}

package com.pig4cloud.pig.dc.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 出库请求：可为每个备件指定来源服务器（真实序列号或「无」）。
 */
@Data
public class AccessoryOutboundRequest {

    private String remark;

    private List<OutboundSourceItem> sources = new ArrayList<>();

    @Data
    public static class OutboundSourceItem {

        private Long sparePartId;

        private Long sourceServerId;

        /** 来源服务器序列号；填「无」表示无具体来源服务器 */
        private String sourceServerSerial;

        private String sourceServerName;
    }
}

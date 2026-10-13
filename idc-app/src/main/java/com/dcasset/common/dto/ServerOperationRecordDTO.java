package com.dcasset.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServerOperationRecordDTO {

    private Long id;
    private Long serverId;
    private LocalDateTime modifiedAt;
    private String changeContent;
    private String changeReason;
}

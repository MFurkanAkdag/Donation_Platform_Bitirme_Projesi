package com.seffafbagis.api.dto.response.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EmailLogResponse {
    private UUID id;
    private String emailTo;
    private String emailType;
    private String subject;
    private String status;
    private LocalDateTime sentAt;
    private String errorMessage;
}

package com.seffafbagis.api.dto.response.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for notification response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private String type;
    private String typeName;
    private String title;
    private String message;
    private String entityType;
    private UUID entityId;
    private String actionUrl;
    private Map<String, Object> data;
    private Boolean isRead;
    private OffsetDateTime createdAt;
    private String timeAgo;
}

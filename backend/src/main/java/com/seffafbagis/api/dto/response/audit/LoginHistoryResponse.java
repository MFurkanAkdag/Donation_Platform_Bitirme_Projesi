package com.seffafbagis.api.dto.response.audit;

import com.seffafbagis.api.entity.auth.LoginHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String loginStatus;
    private String ipAddress;
    private String userAgent;
    private String deviceType;
    private String locationCountry;
    private String locationCity;
    private String failureReason;
    private Instant createdAt;

    public static LoginHistoryResponse fromEntity(LoginHistory history) {
        if (history == null) {
            return null;
        }
        return LoginHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser() != null ? history.getUser().getId() : null)
                .userEmail(history.getUser() != null ? history.getUser().getEmail() : null)
                .loginStatus(history.getLoginStatus())
                .ipAddress(history.getIpAddress())
                .userAgent(history.getUserAgent())
                .deviceType(history.getDeviceType())
                .locationCountry(history.getLocationCountry())
                .locationCity(history.getLocationCity())
                .failureReason(history.getFailureReason())
                .createdAt(history.getCreatedAt() != null ? history.getCreatedAt().toInstant() : null)
                .build();
    }
}

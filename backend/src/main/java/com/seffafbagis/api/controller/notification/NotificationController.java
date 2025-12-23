package com.seffafbagis.api.controller.notification;

import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.dto.response.notification.EmailLogResponse;
import com.seffafbagis.api.dto.response.notification.NotificationCountResponse;
import com.seffafbagis.api.dto.response.notification.NotificationListResponse;
import com.seffafbagis.api.entity.notification.EmailLog;
import com.seffafbagis.api.repository.EmailLogRepository;
import com.seffafbagis.api.service.notification.EmailService;
import com.seffafbagis.api.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final EmailLogRepository emailLogRepository;
    private final EmailService emailService;

    // --- User Endpoints ---

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getMyNotifications(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(pageable)));
    }

    @GetMapping("/notifications/unread")
    public ResponseEntity<ApiResponse<NotificationListResponse>> getUnreadNotifications(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadNotifications(pageable)));
    }

    @GetMapping("/notifications/count")
    public ResponseEntity<ApiResponse<NotificationCountResponse>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadCount()));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- Admin Endpoints ---

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/notifications/email-logs")
    public ResponseEntity<ApiResponse<Page<EmailLogResponse>>> getEmailLogs(Pageable pageable) {
        Page<EmailLog> logs = emailLogRepository.findAll(pageable); // Using findAll for simplicity, allows filtering
                                                                    // later
        Page<EmailLogResponse> response = logs.map(log -> EmailLogResponse.builder()
                .id(log.getId())
                .emailTo(log.getEmailTo())
                .emailType(log.getEmailType())
                .subject(log.getSubject())
                .status(log.getStatus())
                .sentAt(log.getSentAt())
                .errorMessage(log.getErrorMessage())
                .build());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/notifications/retry-failed")
    public ResponseEntity<ApiResponse<Void>> retryFailedEmails() {
        emailService.retryFailedEmails();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/notifications/broadcast")
    public ResponseEntity<ApiResponse<Void>> broadcastNotification(@RequestParam String title,
            @RequestParam String message) {
        notificationService.notifyAllUsers(title, message);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

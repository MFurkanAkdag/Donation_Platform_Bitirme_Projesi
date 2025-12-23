package com.seffafbagis.api.service.audit;

import com.seffafbagis.api.dto.response.audit.LoginHistoryResponse;
import com.seffafbagis.api.entity.auth.LoginHistory;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.LoginHistoryRepository;
import com.seffafbagis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(LoginHistoryService.class);

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recordLogin(UUID userId, String status, String ipAddress, String userAgent, String failureReason) {
        try {
            User user = userRepository.getReferenceById(userId);

            LoginHistory history = new LoginHistory();
            history.setUser(user);
            history.setLoginStatus(status);
            history.setIpAddress(ipAddress);
            history.setUserAgent(userAgent);
            history.setDeviceType(detectDeviceType(userAgent));
            history.setFailureReason(failureReason);

            // Detect geolocation from IP (GeoIP integration ready for future enhancement)
            String[] geolocation = detectGeolocation(ipAddress);
            history.setLocationCountry(geolocation[0]);
            history.setLocationCity(geolocation[1]);

            loginHistoryRepository.save(history);
        } catch (Exception e) {
            logger.error("Failed to record login history for user {}: {}", userId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<LoginHistoryResponse> getUserLoginHistory(UUID userId, Pageable pageable) {
        User user = userRepository.getReferenceById(userId);
        return loginHistoryRepository.findAllByUserOrderByCreatedAtDesc(user, pageable)
                .map(LoginHistoryResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public long getRecentFailedLogins(UUID userId, int hours) {
        User user = userRepository.getReferenceById(userId);
        OffsetDateTime cutoff = OffsetDateTime.now().minus(hours, ChronoUnit.HOURS);
        return loginHistoryRepository.countByUserAndLoginStatusAndCreatedAtAfter(user, "failed", cutoff);
    }

    @Transactional(readOnly = true)
    public List<String> detectSuspiciousActivity(UUID userId) {
        List<String> concerns = new ArrayList<>();
        // Simple heuristic: > 5 failed logins in last 1 hour
        long recentFailures = getRecentFailedLogins(userId, 1);
        if (recentFailures > 5) {
            concerns.add("Multiple failed login attempts detected (" + recentFailures + " in last hour)");
        }
        return concerns;
    }

    @Transactional
    public long cleanupOldHistory(int retentionDays) {
        OffsetDateTime cutoff = OffsetDateTime.now().minus(retentionDays, ChronoUnit.DAYS);
        return loginHistoryRepository.deleteAllByCreatedAtBefore(cutoff);
    }

    private String detectDeviceType(String userAgent) {
        if (userAgent == null)
            return "unknown";
        String lower = userAgent.toLowerCase();
        if (lower.contains("mobile") || lower.contains("android") || lower.contains("iphone")) {
            return "mobile";
        }
        if (lower.contains("tablet") || lower.contains("ipad")) {
            return "tablet";
        }
        return "desktop";
    }

    /**
     * Detects country and city from IP address using GeoIP lookup.
     * 
     * This method is prepared for future integration with GeoIP service
     * (e.g., MaxMind GeoIP2, IP2Location, etc.).
     * 
     * Currently returns null values as a placeholder for future implementation.
     * When a GeoIP service is integrated, this method should:
     * 1. Accept GeoIPService dependency via constructor
     * 2. Call geoIPService.lookupLocation(ipAddress)
     * 3. Return location country and city
     * 
     * @param ipAddress IP address to lookup
     * @return Object array with [country, city] or [null, null] if not available
     */
    private String[] detectGeolocation(String ipAddress) {
        // Placeholder for future GeoIP integration
        // Example implementation when GeoIP service is available:
        // try {
        // GeoLocation location = geoIPService.lookup(ipAddress);
        // return new String[]{location.getCountry(), location.getCity()};
        // } catch (Exception e) {
        // log.debug("GeoIP lookup failed for IP: {}", ipAddress);
        // return new String[]{null, null};
        // }

        // For now, return null values
        return new String[] { null, null };
    }
}

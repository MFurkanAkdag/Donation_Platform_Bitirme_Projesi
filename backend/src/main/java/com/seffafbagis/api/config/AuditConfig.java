package com.seffafbagis.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Enables JPA auditing so {@code @CreatedDate} and {@code @LastModifiedDate} work across entities.
 * The current auditor is derived from the authenticated principal or falls back to "system" for scheduled tasks.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(resolveCurrentAuditor());
    }

    private String resolveCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || isAnonymous(authentication)) {
            return "system";
        }
        return authentication.getName();
    }

    private boolean isAnonymous(Authentication authentication) {
        String name = authentication.getName();
        return name == null || name.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(name);
    }
}

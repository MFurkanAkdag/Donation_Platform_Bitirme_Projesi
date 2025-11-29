package com.seffafbagis.api.config;

import com.seffafbagis.api.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Auditing yapılandırması.
 * 
 * Bu yapılandırma sayesinde entity'lerde:
 * - @CreatedBy: Kaydı oluşturan kullanıcı
 * - @LastModifiedBy: Son güncelleyen kullanıcı
 * - @CreatedDate: Oluşturulma tarihi
 * - @LastModifiedDate: Son güncelleme tarihi
 * 
 * alanları otomatik doldurulur.

 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /**
     * Auditor provider bean'i.
     * 
     * Her kayıt işleminde mevcut kullanıcının ID'sini döndürür.
     * Giriş yapmamış kullanıcılar için boş Optional döner.
     * 
     * @return AuditorAware<UUID> implementasyonu
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * AuditorAware implementasyonu.
     * 
     * SecurityContext'ten mevcut kullanıcıyı alır.
     */
    public static class AuditorAwareImpl implements AuditorAware<UUID> {

        /**
         * Mevcut kullanıcının ID'sini döndürür.
         * 
         * @return Kullanıcı ID'si veya boş Optional
         */
        @Override
        public Optional<UUID> getCurrentAuditor() {
            // SecurityUtils sınıfından mevcut kullanıcı ID'sini al
            UUID currentUserId = SecurityUtils.getCurrentUserId();
            
            // Kullanıcı giriş yapmamışsa boş Optional döndür
            if (currentUserId == null) {
                return Optional.empty();
            }
            
            return Optional.of(currentUserId);
        }
    }
}

package com.seffafbagis.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) yapılandırması.
 * 
 * CORS, farklı domain'lerden gelen istekleri kontrol eder.
 * Frontend (örn: localhost:3000) ve Backend (örn: localhost:8080) 
 * farklı portlarda çalıştığında CORS gereklidir.
 * 
 * GÜVENLİK NOTU:
 * - Production'da allowedOrigins'i sadece kendi domain'inize sınırlayın
 * - "*" kullanmaktan kaçının
 * - Credentials true ise "*" origin kullanamazsınız
 * 
 * @author Furkan
 * @version 1.0
 */
@Configuration
public class CorsConfig {

    /**
     * İzin verilen origin'ler.
     * application.yml'dan okunur.
     * Örnek: http://localhost:3000, https://seffafbagis.com
     */
    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    /**
     * İzin verilen HTTP metodları.
     */
    private static final List<String> ALLOWED_METHODS = Arrays.asList(
        "GET",
        "POST",
        "PUT",
        "PATCH",
        "DELETE",
        "OPTIONS"
    );

    /**
     * İzin verilen HTTP header'ları.
     */
    private static final List<String> ALLOWED_HEADERS = Arrays.asList(
        "Authorization",
        "Content-Type",
        "Accept",
        "Origin",
        "X-Requested-With",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers"
    );

    /**
     * Response'ta expose edilecek header'lar.
     * Frontend bu header'ları okuyabilir.
     */
    private static final List<String> EXPOSED_HEADERS = Arrays.asList(
        "Authorization",
        "Content-Disposition",
        "X-Total-Count",
        "X-Total-Pages"
    );

    /**
     * Preflight cache süresi (saniye).
     * Tarayıcı bu süre boyunca OPTIONS isteği göndermez.
     */
    private static final Long MAX_AGE_SECONDS = 3600L;

    /**
     * CORS yapılandırma kaynağı bean'i.
     * 
     * @return CorsConfigurationSource instance
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // İzin verilen origin'leri ayarla
        configuration.setAllowedOrigins(allowedOrigins);
        
        // İzin verilen HTTP metodları
        configuration.setAllowedMethods(ALLOWED_METHODS);
        
        // İzin verilen header'lar
        configuration.setAllowedHeaders(ALLOWED_HEADERS);
        
        // Expose edilecek header'lar
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        
        // Cookie ve Authorization header gönderimi için gerekli
        // Bu true olduğunda allowedOrigins "*" olamaz
        configuration.setAllowCredentials(true);
        
        // Preflight cache süresi
        configuration.setMaxAge(MAX_AGE_SECONDS);
        
        // Tüm endpoint'lere uygula
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

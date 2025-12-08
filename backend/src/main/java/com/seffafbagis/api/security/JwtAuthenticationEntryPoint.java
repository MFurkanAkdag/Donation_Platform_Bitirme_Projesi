package com.seffafbagis.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point.
 * 
 * Yetkisiz erişim denemelerinde çağrılır.
 * 401 Unauthorized hatası döndürür.
 * 
 * Bu sınıf şu durumlarda devreye girer:
 * - Token gönderilmemiş
 * - Token geçersiz
 * - Token süresi dolmuş
 * - Kullanıcı authenticate olamamış
 * 
 * @author Furkan
 * @version 1.0
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;

    /**
     * Constructor.
     * 
     * @param objectMapper JSON serializer
     */
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Yetkisiz erişim durumunda çağrılır.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param authException Authentication hatası
     * @throws IOException IO hatası
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        logger.debug("Unauthorized request to {} - {}", request.getRequestURI(), authException.getMessage());

        // Response ayarları
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        // Hata mesajı oluştur
        Map<String, Object> errorResponse = createErrorResponse(request, authException);

        // JSON olarak yaz
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    /**
     * Hata yanıtı oluşturur.
     * 
     * @param request HTTP request
     * @param authException Authentication hatası
     * @return Hata yanıtı map
     */
    private Map<String, Object> createErrorResponse(
            HttpServletRequest request,
            AuthenticationException authException) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("error", "UNAUTHORIZED");
        response.put("message", "Authentication required. Please login.");
        response.put("timestamp", Instant.now().toString());
        response.put("path", request.getRequestURI());
        return response;
    }

}

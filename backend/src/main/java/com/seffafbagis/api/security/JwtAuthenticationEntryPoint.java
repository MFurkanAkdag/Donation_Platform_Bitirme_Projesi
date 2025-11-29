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

        // Log
        logger.warn("Yetkisiz erişim denemesi - Path: {}, IP: {}, Hata: {}",
                request.getRequestURI(),
                request.getRemoteAddr(),
                authException.getMessage());

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

        response.put("timestamp", Instant.now().toString());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", getErrorMessage(authException));
        response.put("path", request.getRequestURI());

        return response;
    }

    /**
     * Kullanıcı dostu hata mesajı oluşturur.
     * 
     * @param authException Authentication hatası
     * @return Hata mesajı
     */
    private String getErrorMessage(AuthenticationException authException) {
        String originalMessage = authException.getMessage();

        // Teknik hata mesajlarını kullanıcı dostu hale getir
        if (originalMessage == null) {
            return "Kimlik doğrulama gerekli. Lütfen giriş yapın.";
        }

        // Token ile ilgili hatalar
        if (originalMessage.contains("expired")) {
            return "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.";
        }

        if (originalMessage.contains("invalid") || originalMessage.contains("malformed")) {
            return "Geçersiz kimlik bilgisi. Lütfen tekrar giriş yapın.";
        }

        // Genel hata mesajı
        return "Bu işlem için giriş yapmanız gerekiyor.";
    }
}

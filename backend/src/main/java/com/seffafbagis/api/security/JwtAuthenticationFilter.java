package com.seffafbagis.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter.
 * 
 * Her HTTP isteğinde çalışır ve JWT token'ı doğrular.
 * Geçerli token varsa SecurityContext'e kullanıcıyı ekler.
 * 
 * İşlem akışı:
 * 1. Request'ten Authorization header'ını al
 * 2. "Bearer " prefix'ini kaldır
 * 3. Token'ı doğrula
 * 4. Geçerliyse kullanıcıyı yükle
 * 5. SecurityContext'e authentication ekle
 * 
 * @author Furkan
 * @version 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Authorization header adı.
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer token prefix'i.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor.
     * 
     * @param jwtTokenProvider JWT token işlemleri
     * @param userDetailsService Kullanıcı yükleme servisi
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filter metodu - her request'te çalışır.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter zinciri
     * @throws ServletException Servlet hatası
     * @throws IOException IO hatası
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Token'ı al
            String jwt = extractTokenFromRequest(request);

            // 2. Token yoksa devam et (public endpoint olabilir)
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Token'ı doğrula
            boolean isValidToken = jwtTokenProvider.validateToken(jwt);
            if (!isValidToken) {
                logger.warn("Geçersiz JWT token - IP: {}", request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            // 4. Access token mı kontrol et
            boolean isAccessToken = jwtTokenProvider.isAccessToken(jwt);
            if (!isAccessToken) {
                logger.warn("Refresh token ile erişim denemesi - IP: {}", request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            // 5. Token'dan e-posta al
            String email = jwtTokenProvider.extractEmail(jwt);
            if (email == null) {
                logger.warn("Token'da email bulunamadı - IP: {}", request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            // 6. Zaten authenticate edilmiş mi kontrol et
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 7. Kullanıcıyı yükle
            UserDetails userDetails = loadUserDetails(email);
            if (userDetails == null) {
                logger.warn("Kullanıcı bulunamadı: {} - IP: {}", email, request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            // 8. Kullanıcı aktif mi kontrol et
            if (!userDetails.isEnabled()) {
                logger.warn("Pasif kullanıcı erişim denemesi: {} - IP: {}", email, request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            // 9. Authentication oluştur ve SecurityContext'e ekle
            setAuthentication(userDetails, request);

            logger.debug("Kullanıcı doğrulandı: {}", email);

        } catch (Exception e) {
            logger.error("JWT authentication hatası: {}", e.getMessage());
        }

        // Filter zincirini devam ettir
        filterChain.doFilter(request, response);
    }

    /**
     * Request'ten JWT token'ı çıkarır.
     * 
     * Authorization header'ından "Bearer " prefix'ini kaldırır.
     * 
     * @param request HTTP request
     * @return JWT token veya null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        // Header boş mu kontrol et
        if (!StringUtils.hasText(bearerToken)) {
            return null;
        }

        // "Bearer " ile başlıyor mu kontrol et
        if (!bearerToken.startsWith(BEARER_PREFIX)) {
            return null;
        }

        // "Bearer " prefix'ini kaldır ve token'ı döndür
        return bearerToken.substring(BEARER_PREFIX.length());
    }

    /**
     * Kullanıcı detaylarını yükler.
     * 
     * @param email Kullanıcı e-postası
     * @return UserDetails veya null
     */
    private UserDetails loadUserDetails(String email) {
        try {
            return userDetailsService.loadUserByUsername(email);
        } catch (Exception e) {
            logger.error("Kullanıcı yükleme hatası: {}", e.getMessage());
            return null;
        }
    }

    /**
     * SecurityContext'e authentication ekler.
     * 
     * @param userDetails Kullanıcı detayları
     * @param request HTTP request
     */
    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null, // Credentials (şifre) gerekmez, token ile doğrulandı
                userDetails.getAuthorities()
        );

        // Request detaylarını ekle (IP adresi vb.)
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // SecurityContext'e ekle
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Bu filter'ın hangi request'lerde çalışmayacağını belirler.
     * 
     * Şu an tüm request'lerde çalışıyor.
     * Gerekirse public endpoint'leri buradan exclude edebilirsiniz.
     * 
     * @param request HTTP request
     * @return false (tüm request'lerde çalışsın)
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        // Tüm request'lerde çalışsın
        // Token yoksa filter otomatik olarak pas geçecek
        return false;
    }
}

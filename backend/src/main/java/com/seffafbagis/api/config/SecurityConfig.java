package com.seffafbagis.api.config;

import com.seffafbagis.api.security.JwtAuthenticationEntryPoint;
import com.seffafbagis.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security ana yapılandırma sınıfı.
 * 
 * Bu sınıf şunları yapılandırır:
 * - JWT tabanlı kimlik doğrulama
 * - Stateless session yönetimi
 * - Endpoint bazlı yetkilendirme kuralları
 * - Password encoding (BCrypt)
 * 
 * @author Furkan
 * @version 1.0
 * @since 2024
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    /**
     * Constructor injection kullanıyoruz.
     * Field injection yerine constructor injection tercih edilir çünkü:
     * - Test edilebilirlik artar
     * - Bağımlılıklar açıkça görülür
     * - Immutability sağlanır
     */
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            UserDetailsService userDetailsService) {
        
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Herkesin erişebileceği endpoint'ler.
     * Bu endpoint'ler için JWT token gerekmez.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
        // Auth işlemleri
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/auth/refresh-token",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
        "/api/v1/auth/verify-email",
        
        // Sağlık kontrolü
        "/actuator/health",
        "/actuator/info",
        
        // API dokümantasyonu
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    /**
     * GET istekleri için herkese açık endpoint'ler.
     * Kampanya ve vakıf listeleme herkes tarafından görülebilir.
     */
    private static final String[] PUBLIC_GET_ENDPOINTS = {
        "/api/v1/campaigns",
        "/api/v1/campaigns/**",
        "/api/v1/organizations",
        "/api/v1/organizations/**",
        "/api/v1/categories",
        "/api/v1/categories/**",
        "/api/v1/donation-types",
        "/api/v1/donation-types/**"
    };

    /**
     * Ana güvenlik filtre zinciri yapılandırması.
     * 
     * @param http HttpSecurity nesnesi
     * @return Yapılandırılmış SecurityFilterChain
     * @throws Exception Yapılandırma hatası durumunda
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        // CSRF korumasını devre dışı bırak
        // REST API'lerde CSRF gerekli değil çünkü token tabanlı auth kullanıyoruz
        http.csrf(AbstractHttpConfigurer::disable);
        
        // Session yönetimini stateless yap
        // Her request kendi JWT token'ı ile gelecek, server'da session tutulmayacak
        http.sessionManagement(session -> {
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });
        
        // Yetkisiz erişim durumunda ne yapılacağını belirle
        http.exceptionHandling(exception -> {
            exception.authenticationEntryPoint(jwtAuthenticationEntryPoint);
        });
        
        // Endpoint yetkilendirme kuralları
        http.authorizeHttpRequests(auth -> {
            // Public endpoint'ler - herkes erişebilir
            auth.requestMatchers(PUBLIC_ENDPOINTS).permitAll();
            
            // GET istekleri için public endpoint'ler
            auth.requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll();
            
            // Admin endpoint'leri - sadece ADMIN rolü
            auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
            
            // Vakıf endpoint'leri - sadece FOUNDATION rolü
            auth.requestMatchers("/api/v1/foundation/**").hasRole("FOUNDATION");
            
            // Bağış yapma - giriş yapmış herkes (DONOR, FOUNDATION, ADMIN)
            auth.requestMatchers(HttpMethod.POST, "/api/v1/donations/**").authenticated();
            
            // Diğer tüm endpoint'ler - giriş gerekli
            auth.anyRequest().authenticated();
        });
        
        // Authentication provider'ı ayarla
        http.authenticationProvider(authenticationProvider());
        
        // JWT filtresini UsernamePasswordAuthenticationFilter'dan önce ekle
        // Bu sayede her request'te önce JWT kontrol edilir
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * Authentication provider yapılandırması.
     * Kullanıcı doğrulama ve şifre kontrolü için kullanılır.
     * 
     * @return Yapılandırılmış AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        // Kullanıcı bilgilerini yüklemek için service
        authProvider.setUserDetailsService(userDetailsService);
        
        // Şifre doğrulama için encoder
        authProvider.setPasswordEncoder(passwordEncoder());
        
        return authProvider;
    }

    /**
     * Authentication manager bean'i.
     * Login işlemlerinde kullanılır.
     * 
     * @param config AuthenticationConfiguration
     * @return AuthenticationManager instance
     * @throws Exception Yapılandırma hatası durumunda
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder bean'i.
     * 
     * BCrypt kullanıyoruz çünkü:
     * - Otomatik salt ekleme
     * - Ayarlanabilir güç faktörü (strength)
     * - Endüstri standardı
     * 
     * Strength 12 kullanıyoruz (varsayılan 10).
     * Daha yüksek değer = daha güvenli ama daha yavaş.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength: 12 (2^12 = 4096 iterasyon)
        // 10-12 arası önerilen değerdir
        return new BCryptPasswordEncoder(12);
    }
}

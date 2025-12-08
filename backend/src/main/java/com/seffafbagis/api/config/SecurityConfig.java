package com.seffafbagis.api.config;

import com.seffafbagis.api.security.CustomUserDetailsService;
import com.seffafbagis.api.security.JwtAuthenticationEntryPoint;
import com.seffafbagis.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
    private final CustomUserDetailsService userDetailsService;

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
            CustomUserDetailsService userDetailsService) {
        
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Herkesin erişebileceği endpoint'ler.
     * Bu endpoint'ler için JWT token gerekmez.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/refresh",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
        "/api/v1/auth/verify-email",
        "/api/v1/settings/public",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/swagger-ui.html",
        "/actuator/health"
    };

    /**
     * GET istekleri için herkese açık endpoint'ler.
     * Kampanya ve vakıf listeleme herkes tarafından görülebilir.
     */
    private static final String[] PUBLIC_GET_ENDPOINTS = {
        "/api/v1/campaigns/**",
        "/api/v1/organizations/**",
        "/api/v1/categories/**"
    };

    private static final String[] ADMIN_ENDPOINTS = {
        "/api/v1/admin/**"
    };

    private static final String[] FOUNDATION_ENDPOINTS = {
        "/api/v1/foundation/**"
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
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                .requestMatchers(ADMIN_ENDPOINTS).hasRole("ADMIN")
                .requestMatchers(FOUNDATION_ENDPOINTS).hasRole("FOUNDATION")
                .anyRequest().authenticated())
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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

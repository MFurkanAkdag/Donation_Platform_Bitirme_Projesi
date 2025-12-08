package com.seffafbagis.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configures global Cross-Origin Resource Sharing rules so that the frontend can access the API.
 * Values are fully driven by application properties which makes it easy to update per environment.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CorsConfig.class);

    private final List<String> allowedOrigins;
    private final List<String> allowedMethods;
    private final List<String> allowedHeaders;
    private final boolean allowCredentials;
    private final long maxAgeSeconds;

    public CorsConfig(
            @Value("${app.cors.allowed-origins}") String origins,
            @Value("${app.cors.allowed-methods}") String methods,
            @Value("${app.cors.allowed-headers}") String headers,
            @Value("${app.cors.allow-credentials:true}") boolean allowCredentials,
            @Value("${app.cors.max-age:3600}") long maxAgeSeconds) {
        this.allowedOrigins = splitToList(origins);
        this.allowedMethods = splitToList(methods);
        this.allowedHeaders = splitToList(headers.equals("*") ? "*" : headers);
        this.allowCredentials = allowCredentials;
        this.maxAgeSeconds = maxAgeSeconds;

        LOGGER.info("Configured CORS with origins: {}", this.allowedOrigins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods(allowedMethods.toArray(new String[0]))
                .allowedHeaders(resolveAllowedHeaders())
                .allowCredentials(allowCredentials)
                .maxAge(maxAgeSeconds);
    }

    private String[] resolveAllowedHeaders() {
        if (allowedHeaders.size() == 1 && "*".equals(allowedHeaders.get(0))) {
            return new String[]{"*"};
        }
        return allowedHeaders.toArray(new String[0]);
    }

    private List<String> splitToList(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList());
    }
}

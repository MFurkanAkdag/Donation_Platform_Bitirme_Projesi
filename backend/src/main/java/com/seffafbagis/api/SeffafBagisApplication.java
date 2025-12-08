package com.seffafbagis.api;

import com.seffafbagis.api.config.JwtConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

import java.util.Arrays;

/**
 * Main Spring Boot entry point for the Transparent Donation Platform backend.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtConfig.class)
public class SeffafBagisApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeffafBagisApplication.class);

    public static void main(String[] args) {
        Environment environment = SpringApplication.run(SeffafBagisApplication.class, args).getEnvironment();
        LOGGER.info("Started SeffafBagisApplication with profiles: {}", Arrays.toString(environment.getActiveProfiles()));
        LOGGER.info("Application name: {}", environment.getProperty("spring.application.name"));
    }
}

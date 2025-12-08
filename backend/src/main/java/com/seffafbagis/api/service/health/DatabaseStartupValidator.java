package com.seffafbagis.api.service.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Executes a database connectivity check during application startup so failures are visible early.
 */
@Component
public class DatabaseStartupValidator implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseStartupValidator.class);

    private final DatabaseHealthService databaseHealthService;

    public DatabaseStartupValidator(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            databaseHealthService.verifyConnectivity();
        } catch (Exception ex) {
            LOGGER.error("Database startup check failed", ex);
            throw ex;
        }
    }
}

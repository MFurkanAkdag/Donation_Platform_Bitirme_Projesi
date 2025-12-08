package com.seffafbagis.api.service.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Provides a simple way to verify PostgreSQL connectivity.
 */
@Service
public class DatabaseHealthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHealthService.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Runs a lightweight "SELECT NOW()" query to ensure the pool can reach PostgreSQL.
     */
    public void verifyConnectivity() {
        jdbcTemplate.queryForObject("SELECT NOW()", java.sql.Timestamp.class);
        LOGGER.info("Database connectivity OK (SELECT NOW()).");
    }

    /**
     * Returns {@code true} if a trivial query executes successfully.
     */
    public boolean isDatabaseUp() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (DataAccessException ex) {
            LOGGER.error("Database health check failed: {}", ex.getMessage());
            return false;
        }
    }
}

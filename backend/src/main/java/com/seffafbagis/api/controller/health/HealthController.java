package com.seffafbagis.api.controller.health;

import com.seffafbagis.api.service.health.DatabaseHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes lightweight health-check endpoints.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private final DatabaseHealthService databaseHealthService;

    public HealthController(DatabaseHealthService databaseHealthService) {
        this.databaseHealthService = databaseHealthService;
    }

    @GetMapping("/db")
    public ResponseEntity<Map<String, String>> databaseHealth() {
        boolean up = databaseHealthService.isDatabaseUp();
        if (up) {
            return ResponseEntity.ok(Map.of("db", "up"));
        }
        return ResponseEntity.internalServerError().body(Map.of("db", "down"));
    }
}

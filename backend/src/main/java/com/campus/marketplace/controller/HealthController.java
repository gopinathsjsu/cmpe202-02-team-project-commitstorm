package com.campus.marketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint for container orchestration and monitoring.
 */
@RestController
@RequestMapping("/api")
public class HealthController {
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "campus-marketplace-api");
        
        // Check database connectivity
        Map<String, Object> db = new HashMap<>();
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection()) {
                boolean valid = conn.isValid(2);
                db.put("status", valid ? "UP" : "DOWN");
                db.put("connected", valid);
            } catch (Exception e) {
                db.put("status", "DOWN");
                db.put("error", e.getMessage());
            }
        } else {
            db.put("status", "UNKNOWN");
        }
        health.put("database", db);
        
        return ResponseEntity.ok(health);
    }
}

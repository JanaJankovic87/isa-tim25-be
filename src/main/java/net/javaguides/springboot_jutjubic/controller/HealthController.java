package net.javaguides.springboot_jutjubic.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    // Ako koristiš RabbitMQ
    // @Autowired(required = false)
    // private ConnectionFactory rabbitConnectionFactory;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("instance", getInstanceName());

        // Proveri bazu
        try (Connection conn = dataSource.getConnection()) {
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN - " + e.getMessage());
            health.put("status", "DEGRADED");
        }

        // Ovde dodaj proveru za MQ ako koristiš
        // try {
        //     Connection conn = rabbitConnectionFactory.createConnection();
        //     conn.close();
        //     health.put("messageQueue", "UP");
        // } catch (Exception e) {
        //     health.put("messageQueue", "DOWN");
        //     health.put("status", "DEGRADED");
        // }

        return ResponseEntity.ok(health);
    }

    private String getInstanceName() {
        // Iz environment variable ili hostname
        String instance = System.getenv("INSTANCE_NAME");
        if (instance == null) {
            instance = System.getenv("HOSTNAME");
        }
        return instance != null ? instance : "unknown";
    }
}
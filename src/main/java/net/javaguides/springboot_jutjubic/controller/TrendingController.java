package net.javaguides.springboot_jutjubic.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.service.impl.GeolocationService;
import net.javaguides.springboot_jutjubic.service.impl.LocalTrendingService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/trending")
@CrossOrigin(origins = "http://localhost:4200")
public class TrendingController {

    private static final Logger logger = LoggerFactory.getLogger(TrendingController.class);

    @Autowired
    private LocalTrendingService localTrendingService;

    @Autowired
    private GeolocationService geolocationService;

    @GetMapping("/local")
    public ResponseEntity<?> getLocalTrending(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "50") int radiusKm,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        LocationDTO userLocation = null;

        // SCENARIO 1: Korisnik ODOBRIO browser lokaciju (frontend poslao lat/lng)
        if (lat != null && lng != null) {
            logger.info("✓ Using browser geolocation: lat={}, lng={}", lat, lng);
            userLocation = new LocationDTO(lat, lng, false);
            userLocation.setLocationName("Browser location");
        }
        // SCENARIO 2: Korisnik ODBIO browser lokaciju → koristi IP geolocation
        else {
            String ipAddress = extractClientIP(request);
            logger.info("User denied geolocation, attempting IP geolocation for: {}", ipAddress);

            userLocation = geolocationService.getLocationFromIP(ipAddress);

            if (userLocation != null) {
                logger.info("✓ Using IP geolocation: {}", userLocation.getLocationName());
            } else {
                logger.error("✗ IP geolocation failed for IP: {}", ipAddress);

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Location unavailable");
                errorResponse.put("message", "Cannot determine location from IP: " + ipAddress);

                return ResponseEntity.status(400).body(errorResponse);
            }
        }

        try {
            LocalTrendingService.TrendingResult result = localTrendingService.getLocalTrending(
                    userLocation,
                    radiusKm,
                    limit
            );

            logger.info("✓ Trending videos returned: {} videos", result.getVideos().size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error fetching local trending: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body("Failed to fetch trending videos");
        }
    }

    @GetMapping("/metrics")
    public ResponseEntity<LocalTrendingService.PerformanceMetrics> getMetrics() {
        return ResponseEntity.ok(localTrendingService.getMetrics());
    }

    @PostMapping("/metrics/reset")
    public ResponseEntity<Void> resetMetrics() {
        localTrendingService.resetMetrics();
        return ResponseEntity.ok().build();
    }

    private String extractClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "127.0.0.1";
    }
}
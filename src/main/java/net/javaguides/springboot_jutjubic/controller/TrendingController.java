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

    /**
     * GLAVNI ENDPOINT - Lokalni trending sa default strategijom (CACHED_60S)
     */
    @GetMapping("/local")
    public ResponseEntity<?> getLocalTrending(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "50") int radiusKm,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        LocationDTO userLocation = getUserLocation(lat, lng, request);
        if (userLocation == null) {
            return ResponseEntity.status(400).body("Location unavailable");
        }

        try {
            // Default: koristi CACHED_60S strategiju (optimalna mera)
            LocalTrendingService.TrendingResult result = localTrendingService.getCachedTrending60s(
                    userLocation,
                    radiusKm,
                    limit
            );

            logger.info("✓ Trending videos returned: {} videos", result.getVideos().size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error fetching local trending: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to fetch trending videos");
        }
    }

    /**
     * PERFORMANCE TESTING - Test specific caching strategy
     */
    @GetMapping("/test-strategy")
    public ResponseEntity<?> testStrategy(
            @RequestParam String strategy,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "50") int radiusKm,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        LocationDTO userLocation = getUserLocation(lat, lng, request);
        if (userLocation == null) {
            return ResponseEntity.status(400).body("Location unavailable");
        }

        LocalTrendingService.TrendingResult result;

        switch (strategy.toUpperCase()) {
            case "REAL_TIME":
                result = localTrendingService.getRealTimeTrending(userLocation, radiusKm, limit);
                break;
            case "CACHED_30S":
                result = localTrendingService.getCachedTrending30s(userLocation, radiusKm, limit);
                break;
            case "CACHED_60S":
                result = localTrendingService.getCachedTrending60s(userLocation, radiusKm, limit);
                break;
            case "CACHED_5MIN":
                result = localTrendingService.getCachedTrending5min(userLocation, radiusKm, limit);
                break;
            default:
                return ResponseEntity.status(400).body("Unknown strategy: " + strategy);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * PERFORMANCE TESTING - Run complete performance test
     */
    @GetMapping("/performance-test")
    public ResponseEntity<?> runPerformanceTest(
            @RequestParam(defaultValue = "50") int iterations,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            HttpServletRequest request) {

        LocationDTO userLocation = getUserLocation(lat, lng, request);
        if (userLocation == null) {
            return ResponseEntity.status(400).body("Location unavailable");
        }

        logger.info("Starting performance test with {} iterations", iterations);

        // Reset metrics before test
        localTrendingService.resetMetrics();

        // Run each strategy
        String[] strategies = {"REAL_TIME", "CACHED_30S", "CACHED_60S", "CACHED_5MIN"};

        for (String strategy : strategies) {
            for (int i = 0; i < iterations; i++) {
                try {
                    switch (strategy) {
                        case "REAL_TIME":
                            localTrendingService.getRealTimeTrending(userLocation, 50, 10);
                            break;
                        case "CACHED_30S":
                            localTrendingService.getCachedTrending30s(userLocation, 50, 10);
                            break;
                        case "CACHED_60S":
                            localTrendingService.getCachedTrending60s(userLocation, 50, 10);
                            break;
                        case "CACHED_5MIN":
                            localTrendingService.getCachedTrending5min(userLocation, 50, 10);
                            break;
                    }
                } catch (Exception e) {
                    logger.error("Error in iteration {} for strategy {}", i, strategy, e);
                }
            }
        }

        logger.info("✓ Performance test completed");
        return ResponseEntity.ok(localTrendingService.getMetrics());
    }

    /**
     * Get performance metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<LocalTrendingService.PerformanceMetrics> getMetrics() {
        return ResponseEntity.ok(localTrendingService.getMetrics());
    }

    /**
     * Reset metrics
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<Void> resetMetrics() {
        localTrendingService.resetMetrics();
        return ResponseEntity.ok().build();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get user location from GPS or IP
     */
    private LocationDTO getUserLocation(Double lat, Double lng, HttpServletRequest request) {
        // SCENARIO 1: Korisnik ODOBRIO browser lokaciju
        if (lat != null && lng != null) {
            logger.info("✓ Using browser geolocation: lat={}, lng={}", lat, lng);
            LocationDTO location = new LocationDTO(lat, lng, false);
            location.setLocationName("Browser location");
            return location;
        }

        // SCENARIO 2: Korisnik ODBIO browser lokaciju → koristi IP geolocation
        String ipAddress = extractClientIP(request);
        logger.info("Using IP geolocation for: {}", ipAddress);
        return geolocationService.getLocationFromIP(ipAddress);
    }

    /**
     * Extract client IP from request headers
     */
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
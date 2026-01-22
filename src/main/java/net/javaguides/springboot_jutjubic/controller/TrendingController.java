package net.javaguides.springboot_jutjubic.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.service.impl.GeolocationService;
import net.javaguides.springboot_jutjubic.service.impl.LocalTrendingService;
import net.javaguides.springboot_jutjubic.service.UserService;

@RestController
@RequestMapping("/api/trending")
@CrossOrigin(origins = "http://localhost:4200")
public class TrendingController {

    private static final Logger logger = LoggerFactory.getLogger(TrendingController.class);

    @Autowired
    private LocalTrendingService localTrendingService;

    @Autowired
    private GeolocationService geolocationService;

    @Autowired
    private UserService userService;

    /**
     * S2: LOKALNI TRENDING
     * Prioriteti:
     * 1. User-provided lat/lng (query params)
     * 2. Authenticated user's Address
     * 3. IP geolocation
     */
    @GetMapping("/local")
    public ResponseEntity<?> getLocalTrending(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "50") int radiusKm,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {

        LocationDTO userLocation = null;

        // PRIORITET 1: User dao lat/lng
        if (lat != null && lng != null) {
            logger.info("✓ User provided location: lat={}, lng={}", lat, lng);
            userLocation = new LocationDTO(lat, lng, false);
            userLocation.setLocationName("User provided");
        }
        // PRIORITET 2: Authenticated user sa Address-om
        else {
            User currentUser = getCurrentUser();

            if (currentUser != null && currentUser.getAddress() != null) {
                logger.info("✓ Using user address: {}", currentUser.getEmail());
                userLocation = geolocationService.getLocationFromAddress(currentUser.getAddress());
            }

            // PRIORITET 3: IP geolocation
            if (userLocation == null) {
                String ipAddress = extractClientIP(request);
                logger.info("✗ Using IP geolocation: {}", ipAddress);
                userLocation = geolocationService.getLocationFromIP(ipAddress);
            }
        }

        // Ako ni posle svega nemamo lokaciju
        if (userLocation == null) {
            logger.error("Could not determine user location");
            return ResponseEntity.badRequest().body("Could not determine your location");
        }

        // Pozovi trending service
        LocalTrendingService.TrendingResult result = localTrendingService.getLocalTrending(
                userLocation,
                radiusKm,
                limit
        );

        return ResponseEntity.ok(result);
    }

    /**
     * S2: Performance metrics endpoint
     */
    @GetMapping("/metrics")
    public ResponseEntity<LocalTrendingService.PerformanceMetrics> getMetrics() {
        return ResponseEntity.ok(localTrendingService.getMetrics());
    }

    /**
     * S2: Reset metrics (za testiranje)
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<Void> resetMetrics() {
        localTrendingService.resetMetrics();
        return ResponseEntity.ok().build();
    }

    /**
     * Uzmi trenutno autentifikovanog korisnika
     */
    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                String email = auth.getName();
                return userService.findByEmail(email);
            }
        } catch (Exception e) {
            logger.warn("Could not get current user: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract client IP address
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
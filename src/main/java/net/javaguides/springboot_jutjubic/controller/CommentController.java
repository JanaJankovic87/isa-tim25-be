package net.javaguides.springboot_jutjubic.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.javaguides.springboot_jutjubic.dto.CommentDTO;
import net.javaguides.springboot_jutjubic.dto.LocationDTO;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.service.CommentRateLimitService;
import net.javaguides.springboot_jutjubic.service.CommentService;
import net.javaguides.springboot_jutjubic.service.UserService;
import net.javaguides.springboot_jutjubic.service.impl.GeolocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/videos/{videoId}/comments")
@CrossOrigin(origins = "http://localhost:4200")
public class CommentController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentRateLimitService rateLimitService;

    @Autowired
    private GeolocationService geolocationService;

    private LocationDTO resolveLocation(Double lat, Double lng, HttpServletRequest request) {
        // sa gps
        if (lat != null && lng != null) {
            LocationDTO location = new LocationDTO(lat, lng, false);
            location.setLocationName("GPS location");
            return location;
        }

        // ip geolocation
        String ipAddress = extractClientIP(request);

        LocationDTO location = geolocationService.getLocationFromIP(ipAddress);

        if (location == null) {
            return null;
        }

        return location;
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

    @PostMapping
    public ResponseEntity<?> createComment(
            @PathVariable Long videoId,
            @Valid @RequestBody CommentDTO commentDTO,
            HttpServletRequest request) {

        try {
            User currentUser = getCurrentUser();
            logger.info("User {} attempting to comment on video {}", currentUser.getUsername(), videoId);

            int remaining = rateLimitService.getRemainingComments(currentUser.getId());

            if (remaining <= 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Comment limit exceeded. You can post 60 comments per hour.");
                error.put("remainingComments", 0);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
            }

            LocationDTO location;
            if (commentDTO.getLatitude() != null && commentDTO.getLongitude() != null) {
                location = new LocationDTO(
                    commentDTO.getLatitude(),
                    commentDTO.getLongitude(),
                    false
                );
                location.setLocationName(commentDTO.getLocationName() != null ?
                    commentDTO.getLocationName() : "GPS Location");
            } else {
                String ipAddress = extractClientIP(request);
                location = geolocationService.getLocationFromIP(ipAddress);
            }

            CommentDTO createdComment = commentService.createComment(videoId, commentDTO, currentUser.getId(), location);

            Map<String, Object> response = new HashMap<>();
            response.put("id", createdComment.getId());
            response.put("text", createdComment.getText());
            response.put("createdAt", createdComment.getCreatedAt());
            response.put("videoId", createdComment.getVideoId());
            response.put("user", Map.of(
                    "id", createdComment.getUserId(),
                    "username", createdComment.getUsername(),
                    "firstName", createdComment.getFirstName(),
                    "lastName", createdComment.getLastName()
            ));
            response.put("remainingComments", remaining - 1);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            logger.error("Error creating comment", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "You must be logged in to comment");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> getComments(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CommentDTO> comments = commentService.getCommentsByVideoId(videoId, pageable);

            return ResponseEntity.ok(comments);

        } catch (Exception e) {
            logger.error("Error fetching comments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching comments: " + e.getMessage());
        }
    }



    @GetMapping("/remaining")
    public ResponseEntity<?> getRemainingComments() {
        try {
            User currentUser = getCurrentUser();
            int remaining = rateLimitService.getRemainingComments(currentUser.getId());

            Map<String, Integer> response = new HashMap<>();
            response.put("remainingComments", remaining);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String username = null;

        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        if (username == null) {
            throw new RuntimeException("Cannot determine username");
        }

        User user = userService.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }

        return user;
    }
}
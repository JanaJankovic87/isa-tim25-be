package net.javaguides.springboot_jutjubic.controller;

import net.javaguides.springboot_jutjubic.dto.WatchPartyRoomDTO;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.service.UserService;
import net.javaguides.springboot_jutjubic.service.impl.WatchPartyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watch-party")
public class WatchPartyController {

    private static final Logger logger = LoggerFactory.getLogger(WatchPartyController.class);

    @Autowired
    private WatchPartyService watchPartyService;

    @Autowired
    private UserService userService;

    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom() {
        try {
            User user = getCurrentUser();
            WatchPartyRoomDTO room = watchPartyService.createRoom(user.getId(), user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(room);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Must be logged in to create a room"));
        }
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<WatchPartyRoomDTO>> getAllRooms() {
        return ResponseEntity.ok(watchPartyService.getAllActiveRooms());
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        try {
            return ResponseEntity.ok(watchPartyService.getRoom(roomId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Room not found: " + roomId));
        }
    }

    @PostMapping("/rooms/{roomId}/start")
    public ResponseEntity<?> startVideo(
            @PathVariable String roomId,
            @RequestBody Map<String, Long> body) {
        try {
            Long videoId = body.get("videoId");
            if (videoId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "videoId is required"));
            }
            User user = getCurrentUser();
            watchPartyService.startVideo(roomId, videoId, user.getId(), user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Video started", "roomId", roomId, "videoId", videoId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> closeRoom(@PathVariable String roomId) {
        try {
            User user = getCurrentUser();
            watchPartyService.closeRoom(roomId, user.getId(), user.getUsername());
            return ResponseEntity.ok(Map.of("message", "Room closed"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }

        String username = null;
        if (auth.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) auth.getPrincipal()).getUsername();
        } else if (auth.getPrincipal() instanceof String) {
            username = (String) auth.getPrincipal();
        }

        if (username == null) throw new RuntimeException("Cannot determine username");

        User user = userService.findByUsername(username);
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }
}
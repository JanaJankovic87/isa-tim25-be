package net.javaguides.springboot_jutjubic.controller;

import net.javaguides.springboot_jutjubic.dto.UserProfileDTO;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        User user = userService.findById(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        UserProfileDTO profile = new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        );

        return ResponseEntity.ok(profile);
    }
}
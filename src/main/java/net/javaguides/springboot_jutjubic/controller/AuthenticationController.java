package net.javaguides.springboot_jutjubic.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import net.javaguides.springboot_jutjubic.dto.JwtAuthenticationRequest;
import net.javaguides.springboot_jutjubic.dto.UserRequest;
import net.javaguides.springboot_jutjubic.dto.UserTokenState;
import net.javaguides.springboot_jutjubic.model.User;
import net.javaguides.springboot_jutjubic.service.UserService;
import net.javaguides.springboot_jutjubic.util.TokenUtils;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public ResponseEntity<UserTokenState> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequest authenticationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        String jwt = tokenUtils.generateToken(user.getUsername(), request);

        String deviceType = tokenUtils.getDeviceTypeFromToken(jwt);
        int expiresIn = tokenUtils.getExpiredIn(deviceType);

        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
    }

    @PostMapping("/login/{deviceType}")
    public ResponseEntity<UserTokenState> createAuthenticationTokenForDevice(
            @RequestBody JwtAuthenticationRequest authenticationRequest,
            @PathVariable String deviceType,
            HttpServletResponse response) {

        if (!isValidDeviceType(deviceType)) {
            return ResponseEntity.badRequest().build();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        String jwt = tokenUtils.generateTokenForDevice(user.getUsername(), deviceType, user);
        int expiresIn = tokenUtils.getExpiredIn(deviceType);

        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> addUser(
            @Valid @RequestBody UserRequest userRequest,
            BindingResult result) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        if (!userRequest.getPassword().equals(userRequest.getConfirmPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("confirmPassword", "Lozinke se ne poklapaju");
            return ResponseEntity.badRequest().body(error);
        }

        User existingUserByUsername = this.userService.findByUsername(userRequest.getUsername());
        if (existingUserByUsername != null) {
            Map<String, String> error = new HashMap<>();
            error.put("username", "Korisničko ime već postoji");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        User existingUserByEmail = this.userService.findByEmail(userRequest.getEmail());
        if (existingUserByEmail != null) {
            Map<String, String> error = new HashMap<>();
            error.put("email", "Email adresa je već registrovana");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        String passwordError = validatePasswordStrength(userRequest.getPassword());
        if (passwordError != null) {
            Map<String, String> error = new HashMap<>();
            error.put("password", passwordError);
            return ResponseEntity.badRequest().body(error);
        }

        User user = this.userService.save(userRequest);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    private String validatePasswordStrength(String password) {
        if (password.length() < 8) {
            return "Lozinka mora imati najmanje 8 karaktera";
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpper || !hasLower || !hasDigit) {
            return "Lozinka mora sadržati bar jedno veliko slovo, malo slovo i cifru";
        }

        return null;
    }

    private boolean isValidDeviceType(String deviceType) {
        return "web".equals(deviceType) ||
                "mobile".equals(deviceType) ||
                "tablet".equals(deviceType);
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAuthenticationToken(HttpServletRequest request) {
        String token = tokenUtils.getToken(request);

        if (token == null) {
            return ResponseEntity.badRequest().body("Token nije pronađen");
        }

        try {
            String username = tokenUtils.getUsernameFromToken(token);
            User user = userService.findByUsername(username);

            if (user != null && tokenUtils.validateToken(token, user)) {
                // Generiši novi token sa istim tipom uređaja
                String deviceType = tokenUtils.getDeviceTypeFromToken(token);
                String newToken = tokenUtils.generateTokenForDevice(username, deviceType, user);
                int expiresIn = tokenUtils.getExpiredIn(deviceType);

                return ResponseEntity.ok(new UserTokenState(newToken, expiresIn));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nevažeći token");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token ne može biti osvežen");
    }
}

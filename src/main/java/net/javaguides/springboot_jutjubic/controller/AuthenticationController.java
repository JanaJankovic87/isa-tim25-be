package net.javaguides.springboot_jutjubic.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.javaguides.springboot_jutjubic.service.EmailService;
import net.javaguides.springboot_jutjubic.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequest authenticationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        String ipAddress = getClientIpAddress(request);

        try {
            Integer attempts = loginAttemptService.getAttempts(ipAddress);
            if (attempts != null && attempts >= 5) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Too many failed login attempts. Please try again after 60 seconds.");
                error.put("remainingAttempts", 0);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
            }
        } catch (Exception e) {
            System.err.println("Cache error: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            String deviceType = detectDeviceTypeFromRequest(request);
            String jwt = tokenUtils.generateTokenForDevice(user.getEmail(), deviceType, user);
            int expiresIn = tokenUtils.getExpiredIn(deviceType);

            try {
                loginAttemptService.resetAttempts(ipAddress);
            } catch (Exception e) {
                System.err.println("Cache error on reset: " + e.getMessage());
            }

            return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));

        } catch (DisabledException e) {
            int remainingAttempts = updateAttempts(ipAddress);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Account is not activated. Please check your email for activation link.");
            error.put("remainingAttempts", remainingAttempts);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (BadCredentialsException e) {
            int remainingAttempts = 5;
            try {
                Integer currentAttempts = loginAttemptService.getAttempts(ipAddress);

                int newAttempts = (currentAttempts == null) ? 1 : currentAttempts + 1;

                loginAttemptService.updateAttempts(ipAddress, newAttempts);
                remainingAttempts = Math.max(0, 5 - newAttempts);

            } catch (Exception cacheEx) {
                System.err.println("Cache error: " + cacheEx.getMessage());
                cacheEx.printStackTrace();
            }

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Wrong username or password.");
            error.put("remainingAttempts", remainingAttempts);

            if (remainingAttempts == 0) {
                error.put("message", "Too many failed login attempts. Please try again after 60 seconds.");
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/login/{deviceType}")
    public ResponseEntity<?> createAuthenticationTokenForDevice(
            @RequestBody JwtAuthenticationRequest authenticationRequest,
            @PathVariable String deviceType,
            HttpServletRequest request) {

        if (!isValidDeviceType(deviceType)) {
            return ResponseEntity.badRequest().body("Invalid device type");
        }

        String ipAddress = getClientIpAddress(request);

        try {
            Integer attempts = loginAttemptService.getAttempts(ipAddress);
            if (attempts != null && attempts >= 5) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Too many failed login attempts. Please try again after 60 seconds.");
                error.put("remainingAttempts", 0);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
            }
        } catch (Exception e) {
            System.err.println("Cache error: " + e.getMessage());
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            String resolvedDeviceType = deviceType;
            if (resolvedDeviceType == null || resolvedDeviceType.isEmpty() || "unknown".equals(resolvedDeviceType)) {
                resolvedDeviceType = detectDeviceTypeFromRequest(request);
            }
            String jwt = tokenUtils.generateTokenForDevice(user.getEmail(), resolvedDeviceType, user);
            int expiresIn = tokenUtils.getExpiredIn(resolvedDeviceType);

            loginAttemptService.resetAttempts(ipAddress);

            return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));

        } catch (DisabledException e) {
            int remainingAttempts = updateAttempts(ipAddress);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Account is not activated. Please check your email.");
            error.put("remainingAttempts", remainingAttempts);

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);

        } catch (BadCredentialsException e) {
            int remainingAttempts = updateAttempts(ipAddress);

            Map<String, Object> error = new HashMap<>();
            error.put("error", "Wrong username or password.");
            error.put("remainingAttempts", remainingAttempts);

            if (remainingAttempts == 0) {
                error.put("message", "Too many failed login attempts. Please try again after 60 seconds.");
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
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
            error.put("confirmPassword", "Password confirmation does not match");
            return ResponseEntity.badRequest().body(error);
        }

        User existingUserByUsername = this.userService.findByUsername(userRequest.getUsername());
        if (existingUserByUsername != null) {
            Map<String, String> error = new HashMap<>();
            error.put("username", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        User existingUserByEmail = this.userService.findByEmail(userRequest.getEmail());
        if (existingUserByEmail != null) {
            Map<String, String> error = new HashMap<>();
            error.put("email", "Email already in use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        String passwordError = validatePasswordStrength(userRequest.getPassword());
        if (passwordError != null) {
            Map<String, String> error = new HashMap<>();
            error.put("password", passwordError);
            return ResponseEntity.badRequest().body(error);
        }

        try {
            User user = this.userService.save(userRequest);

            String token = user.getVerificationToken();

            if (token != null && !token.isEmpty()) {
                emailService.sendVerificationEmail(user.getEmail(), token, user.getUsername());
            }
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful. Please check your email for verification.");

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("ERROR during signup: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private String validatePasswordStrength(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        if (!hasUpper || !hasLower || !hasDigit) {
            return "Password must contain at least one uppercase letter, one lowercase letter, and one digit";
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
            User user = userService.findByEmail(username);

            if (user != null && tokenUtils.validateToken(token, user)) {
                String deviceType = tokenUtils.getDeviceTypeFromToken(token);
                String newToken = tokenUtils.generateTokenForDevice(user.getEmail(), deviceType, user);
                int expiresIn = tokenUtils.getExpiredIn(deviceType);

                return ResponseEntity.ok(new UserTokenState(newToken, expiresIn));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nevažeći token");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token ne može biti osvežen");
    }

    // Detect device type helper used by login token generation
    private String detectDeviceTypeFromRequest(HttpServletRequest request) {
        if (request == null) {
            return "web";
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "web";
        }

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") ||
                userAgent.contains("android") ||
                userAgent.contains("iphone") ||
                userAgent.contains("windows phone")) {
            return "mobile";
        }

        if (userAgent.contains("tablet") ||
                userAgent.contains("ipad")) {
            return "tablet";
        }

        return "web";
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activateUser(@RequestParam("token") String token) {
        boolean activated = userService.activateUser(token);

        Map<String, String> response = new HashMap<>();

        if (activated) {
            return ResponseEntity.ok("Account successfully activated.");
        } else {
            return ResponseEntity.ok("Link for account activation is invalid or expired.");
        }
    }

    @GetMapping("/clearLoginAttempts")
    public ResponseEntity<String> clearLoginAttempts() {
        loginAttemptService.removeFromCache();
        return ResponseEntity.ok("All login attempts removed from cache!");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private int updateAttempts(String ipAddress) {
        try {
            Integer currentAttempts = loginAttemptService.getAttempts(ipAddress);
            int newAttempts = (currentAttempts == null) ? 1 : currentAttempts + 1;
            loginAttemptService.updateAttempts(ipAddress, newAttempts);
            return Math.max(0, 5 - newAttempts);
        } catch (Exception e) {
            System.err.println("Cache error: " + e.getMessage());
            return 5;
        }
    }

}

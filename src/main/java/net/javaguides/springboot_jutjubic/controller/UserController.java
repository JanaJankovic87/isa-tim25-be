package net.javaguides.springboot_jutjubic.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.javaguides.springboot_jutjubic.util.TokenUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    private TokenUtils tokenUtils;

    @GetMapping("/home")
    public ResponseEntity<?> userHome() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Samo ulogovani korisnici mogu videti ovu stranicu.");
        resp.put("username", username);
        resp.put("authenticated", auth != null && auth.isAuthenticated());

        return ResponseEntity.ok(resp);
    }


}




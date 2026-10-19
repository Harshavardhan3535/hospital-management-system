package com.hospital.hms.controller;

import com.hospital.hms.dto.LoginRequest;
import com.hospital.hms.dto.LoginResponse;
import com.hospital.hms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @GetMapping("/whoami")
    public ResponseEntity<?> whoami() {
        org.springframework.security.core.Authentication auth =
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null) {
            return ResponseEntity.status(401).body("No authentication in context");
        }
        
        return ResponseEntity.ok(java.util.Map.of(
            "username", auth.getName(),
            "authorities", auth.getAuthorities(),
            "authenticated", auth.isAuthenticated()
        ));
    }
}
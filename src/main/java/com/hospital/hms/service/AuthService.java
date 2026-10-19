package com.hospital.hms.service;

import com.hospital.hms.dto.LoginRequest;
import com.hospital.hms.dto.LoginResponse;
import com.hospital.hms.security.CustomUserDetailsService;
import com.hospital.hms.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            // 1. Authenticate — throws if credentials wrong
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }

        // 2. Load user details
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // 3. Generate token
        String token = jwtService.generateToken(userDetails);

        // 4. Extract roles
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return LoginResponse.builder()
                .token(token)
                .username(userDetails.getUsername())
                .roles(roles)
                .message("Login successful")
                .build();
    }
}
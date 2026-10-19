package com.hospital.hms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        System.out.println("🔍 [FILTER] " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("🔍 [FILTER] Authorization header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⚠️ [FILTER] No Bearer token — skipping JWT processing");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        System.out.println("🔍 [FILTER] Token (first 30 chars): " + jwt.substring(0, Math.min(jwt.length(), 30)));

        String username = null;
        try {
            username = jwtService.extractUsername(jwt);
            System.out.println("👤 [FILTER] Extracted username: " + username);
        } catch (Exception e) {
            System.out.println("❌ [FILTER] Token extraction FAILED: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("👤 [FILTER] Loaded user details for: " + userDetails.getUsername());
            System.out.println("🎭 [FILTER] Authorities: " + userDetails.getAuthorities());

            boolean valid = jwtService.isTokenValid(jwt, userDetails);
            System.out.println("🔐 [FILTER] isTokenValid: " + valid);

            if (valid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ [FILTER] Authentication set in SecurityContext");
            } else {
                System.out.println("❌ [FILTER] Token is NOT valid (username mismatch or expired)");
            }
        } else {
            System.out.println("ℹ️ [FILTER] Skipping — username null or already authenticated");
        }

        filterChain.doFilter(request, response);
    }
}
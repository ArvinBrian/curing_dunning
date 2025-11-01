package com.example.curingdunning.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                email = jwtUtil.extractEmail(jwt);
            } catch (Exception e) {
                // invalid token
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                String role = jwtUtil.extractRole(jwt); // e.g., extracts "customer" or "CUSTOMER"
                
                // 🚨 FIX: Check if the role is null/empty and ensure it is upper-cased for consistency
                if (role != null && !role.isEmpty()) {
                    
                    // Convert to a Spring Security authority format: ROLE_ROLE_NAME
                    // Spring Security requires authorities to be prefixed with ROLE_
                    String authorityString = "ROLE_" + role.toUpperCase(); 

                    // Create the list of authorities
                    List<SimpleGrantedAuthority> authorities = 
                        List.of(new SimpleGrantedAuthority(authorityString));
                    
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Optional: Log an error if the token is valid but the role is missing/empty
                    System.err.println("JWT Token valid but role claim is empty or missing.");
                }
            }
        }

        chain.doFilter(request, response);
    }

}
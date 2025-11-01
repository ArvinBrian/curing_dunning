package com.example.curingdunning.security;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

//import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
@Component
public class JwtUtil {

    private static final String SECRET_KEY = "ThisIsAVeryLongSecretKeyForCuringDunningABCDE"; 
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Updated to include role
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // add role as claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

 // JwtUtil.java (Check this code)
    
 // JwtUtil.java (Add this private helper method)

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
 // JwtUtil.java (Ensure this method exists)

    private Claims extractAllClaims(String token) {
        // ⚠️ Check 2: This is where the verification key is derived.
        return Jwts.parserBuilder()
                   .setSigningKey(key) // <--- Must use the same 'this.key'
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }
    public String extractRole(String token) {
        // Assuming your role claim key is named "role"
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public boolean validateToken(String token) {
        Claims claims = extractClaims(token);
        return claims.getExpiration().after(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

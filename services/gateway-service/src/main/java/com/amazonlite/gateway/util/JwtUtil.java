package com.amazonlite.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Utility class for the API Gateway.
 * Validates JWT tokens at the gateway level before routing requests to downstream services.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private long expirationTime;

    /**
     * Validates a JWT token and extracts claims.
     * 
     * @param token the JWT token to validate
     * @return Claims object if valid
     * @throws JwtException if token is invalid or expired
     */
    public Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("JWT validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the user email from JWT token.
     * 
     * @param token the JWT token
     * @return user email
     */
    public String extractEmail(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * Extracts the user role from JWT token.
     * 
     * @param token the JWT token
     * @return user role
     */
    public String extractRole(String token) {
        return validateToken(token).get("role", String.class);
    }

    /**
     * Checks if token is expired.
     * 
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
}

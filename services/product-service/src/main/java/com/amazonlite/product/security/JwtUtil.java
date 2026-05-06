package com.amazonlite.product.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Utility class for generating and validating JWT tokens.
 * 
 * Handles:
 * - Token generation with user claims
 * - Token validation and expiration checks
 * - Extraction of claims from tokens
 */
@Component
public class JwtUtil {

    // SecretKey for signing JWT tokens (loaded from application.yml)
    private final SecretKey secretKey;

    // Token expiration time in milliseconds (default: 24 hours)
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // Create a SecretKey from the string secret (HMAC-SHA256)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate a JWT token for a given user.
     * 
     * @param userId   the user's ID
     * @param email    the user's email
     * @param role     the user's role
     * @return JWT token string
     */
    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(email)                           // Subject claim (email)
                .claim("userId", userId)                  // Custom claim: user ID
                .claim("role", role)                      // Custom claim: role
                .issuedAt(now)                            // Issued at
                .expiration(expiryDate)                   // Expiration time
                .signWith(secretKey, SignatureAlgorithm.HS256)  // Sign with HS256
                .compact();
    }

    /**
     * Validate a JWT token.
     * 
     * @param token the JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Token is invalid or expired
            return false;
        }
    }

    /**
     * Extract all claims from a JWT token.
     * 
     * @param token the JWT token string
     * @return Claims object containing all claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the email (subject) from a JWT token.
     * 
     * @param token the JWT token string
     * @return the email/subject
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extract the user ID from a JWT token.
     * 
     * @param token the JWT token string
     * @return the user ID
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * Extract the role from a JWT token.
     * 
     * @param token the JWT token string
     * @return the user's role
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }
}

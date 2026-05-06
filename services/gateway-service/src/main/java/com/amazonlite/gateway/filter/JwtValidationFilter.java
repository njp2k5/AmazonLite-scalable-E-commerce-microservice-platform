package com.amazonlite.gateway.filter;

import com.amazonlite.gateway.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;

import java.util.Arrays;
import java.util.List;

/**
 * Gateway Filter for JWT validation.
 * Intercepts all incoming requests to validate JWT tokens in the Authorization header
 * before routing them to downstream services.
 */
@Component
public class JwtValidationFilter extends AbstractGatewayFilterFactory<JwtValidationFilter.Config> {

    private final JwtUtil jwtUtil;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/auth/register",
            "/auth/login"
    );

    public JwtValidationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip JWT validation for public endpoints
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            // Validate JWT for protected endpoints
            try {
                String authHeader = request.getHeaders().getFirst("Authorization");
                
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return respondWithError(exchange, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
                }

                String token = authHeader.substring(7); // Remove "Bearer " prefix
                
                // Validate token
                try {
                    jwtUtil.validateToken(token);
                    
                    // Extract user info and add to request headers for downstream services
                    String email = jwtUtil.extractEmail(token);
                    String role = jwtUtil.extractRole(token);
                    
                    // Create a new request with user headers
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Email", email)
                            .header("X-User-Role", role)
                            .build();
                    
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();
                    
                    return chain.filter(modifiedExchange);
                    
                } catch (JwtException e) {
                    return respondWithError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token: " + e.getMessage());
                }
                
            } catch (Exception e) {
                return respondWithError(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Gateway error: " + e.getMessage());
            }
        };
    }

    /**
     * Checks if the request path matches a public endpoint.
     * 
     * @param path the request path
     * @return true if endpoint is public, false otherwise
     */
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    /**
     * Helper method to send error response.
     * 
     * @param exchange the server web exchange
     * @param status the HTTP status
     * @param message the error message
     * @return Mono representing the response
     */
    private Mono<Void> respondWithError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());
        DataBuffer dataBuffer = response.bufferFactory().wrap(errorResponse.getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * Configuration class for JwtValidationFilter.
     */
    public static class Config {
        // Add configuration properties if needed
    }
}

package com.amazonlite.product.controller;

import com.amazonlite.product.dto.LoginRequest;
import com.amazonlite.product.dto.LoginResponse;
import com.amazonlite.product.dto.RegisterRequest;
import com.amazonlite.product.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller for user registration and login.
 * 
 * Endpoints:
 * - POST /auth/register - Register a new user
 * - POST /auth/login - Login and get JWT token
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     * 
     * Request Body:
     * {
     *   "email": "user@example.com",
     *   "password": "securepassword",
     *   "role": "CUSTOMER"  // Options: CUSTOMER, SELLER, ADMIN
     * }
     * 
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": 1,
     *   "email": "user@example.com",
     *   "role": "CUSTOMER",
     *   "message": "User registered successfully"
     * }
     * 
     * @param request RegisterRequest with email, password, and role
     * @return 201 Created with JWT token and user info
     * @throws IllegalArgumentException if email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body(
                        new LoginResponse(null, null, null, null, "Email is required")
                );
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(
                        new LoginResponse(null, null, null, null, "Password must be at least 6 characters")
                );
            }
            if (request.getRole() == null || request.getRole().isBlank()) {
                request.setRole("CUSTOMER");  // Default role
            }

            // Call AuthService to register user
            LoginResponse response = authService.register(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Email already exists
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new LoginResponse(null, null, null, null, e.getMessage())
            );
        } catch (Exception e) {
            // Unexpected error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new LoginResponse(null, null, null, null, "Registration failed: " + e.getMessage())
            );
        }
    }

    /**
     * Login with email and password.
     * 
     * Request Body:
     * {
     *   "email": "user@example.com",
     *   "password": "securepassword"
     * }
     * 
     * Response:
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": 1,
     *   "email": "user@example.com",
     *   "role": "CUSTOMER",
     *   "message": "Login successful"
     * }
     * 
     * @param request LoginRequest with email and password
     * @return 200 OK with JWT token and user info
     * @throws IllegalArgumentException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body(
                        new LoginResponse(null, null, null, null, "Email is required")
                );
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(
                        new LoginResponse(null, null, null, null, "Password is required")
                );
            }

            // Call AuthService to login user
            LoginResponse response = authService.login(request);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // User not found or invalid password
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new LoginResponse(null, null, null, null, "Invalid credentials: " + e.getMessage())
            );
        } catch (Exception e) {
            // Unexpected error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new LoginResponse(null, null, null, null, "Login failed: " + e.getMessage())
            );
        }
    }

    /**
     * Health check endpoint to verify auth service is running.
     * @return 200 OK with status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Auth service is healthy");
        return ResponseEntity.ok(response);
    }
}

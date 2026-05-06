package com.amazonlite.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 * 
 * Contains:
 * - email: User's email address
 * - password: Plain text password (will be hashed server-side)
 * - role: User role (ADMIN, SELLER, CUSTOMER)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String role;  // Default to "CUSTOMER" if not provided
}

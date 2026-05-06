package com.amazonlite.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for successful authentication (login/register).
 * 
 * Contains:
 * - token: JWT authentication token
 * - userId: Authenticated user's ID
 * - email: User's email
 * - role: User's role (ADMIN, SELLER, CUSTOMER)
 * - message: Success message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String email;
    private String role;
    private String message;
}

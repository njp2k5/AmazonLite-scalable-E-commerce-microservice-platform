package com.amazonlite.product.service;

import com.amazonlite.product.dto.LoginRequest;
import com.amazonlite.product.dto.LoginResponse;
import com.amazonlite.product.dto.RegisterRequest;
import com.amazonlite.product.model.User;
import com.amazonlite.product.repository.UserRepository;
import com.amazonlite.product.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Authentication Service for user registration and login.
 * 
 * Responsibilities:
 * - User registration with password hashing
 * - User login with credentials validation
 * - JWT token generation
 * - Role-based access control support
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Register a new user.
     * 
     * Process:
     * 1. Check if email already exists
     * 2. Hash the password using BCrypt
     * 3. Save user to database with specified role
     * 4. Return JWT token for immediate login
     * 
     * @param request RegisterRequest containing email, password, and role
     * @return LoginResponse with JWT token and user info
     * @throws IllegalArgumentException if email already exists
     */
    public LoginResponse register(RegisterRequest request) {
        // Step 1: Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        // Step 2: Hash the password using BCrypt
        // BCrypt automatically handles salt generation
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Step 3: Create new User entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(hashedPassword);
        user.setRole(User.UserRole.valueOf(request.getRole().toUpperCase()));

        // Save to database
        User savedUser = userRepository.save(user);

        // Step 4: Generate JWT token for immediate login
        String token = jwtUtil.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().toString()
        );

        return new LoginResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().toString(),
                "User registered successfully"
        );
    }

    /**
     * Login an existing user.
     * 
     * Process:
     * 1. Find user by email
     * 2. Verify password using BCrypt
     * 3. Generate JWT token on successful authentication
     * 
     * @param request LoginRequest containing email and password
     * @return LoginResponse with JWT token and user info
     * @throws IllegalArgumentException if credentials are invalid
     */
    public LoginResponse login(LoginRequest request) {
        // Step 1: Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + request.getEmail());
        }

        User user = userOpt.get();

        // Step 2: Verify password using BCrypt
        // passwordEncoder.matches() safely compares plain password with hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password for user: " + request.getEmail());
        }

        // Step 3: Generate JWT token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole().toString(),
                "Login successful"
        );
    }

    /**
     * Verify user role from JWT token.
     * Used for role-based access control in endpoints.
     * 
     * @param token the JWT token
     * @param requiredRole the required role
     * @return true if user has the required role
     */
    public boolean verifyRole(String token, String requiredRole) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        String userRole = jwtUtil.extractRole(token);
        return userRole.equalsIgnoreCase(requiredRole);
    }
}

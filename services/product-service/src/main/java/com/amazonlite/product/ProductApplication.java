package com.amazonlite.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Main Spring Boot application entry point for the Product Service.
 * 
 * Enables:
 * - JPA repositories for database operations
 * - Spring Security for JWT authentication
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.amazonlite.product.repository")
@EnableWebSecurity
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

    /**
     * Bean for password encoding using BCrypt.
     * Used throughout the application for secure password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure HTTP security.
     * 
     * Security Configuration:
     * - /auth/** endpoints are publicly accessible (login/register)
     * - /products endpoints can be protected with role-based checks
     * - CSRF protection disabled for API usage
     * - Stateless session management (JWT doesn't need sessions)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // Disable CSRF for REST API
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/auth/**").permitAll()  // Allow unauthenticated access to /auth
                        .requestMatchers("/health").permitAll()   // Allow health checks
                        .anyRequest().permitAll()  // For now, allow all; later add role checks
                )
                .httpBasic().disable()  // Disable basic auth
                .sessionManagement();

        return http.build();
    }
}


package com.amazonlite.product.config;

import com.amazonlite.product.model.User;
import com.amazonlite.product.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Data Initializer for loading test data on application startup.
 * 
 * Creates the following test users:
 * 1. Admin - Full system access
 * 2. Seller - Can manage products and orders
 * 3. Customer - Can browse and purchase products
 * 
 * Each user has a pre-hashed password for testing.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if test data already exists
        if (userRepository.count() > 0) {
            System.out.println("✓ Test data already initialized. Skipping...");
            return;
        }

        System.out.println("\n==================== Initializing Test Users ====================\n");

        // Create Admin User
        User admin = new User();
        admin.setEmail("admin@amazonlite.com");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));  // Password: admin123
        admin.setRole(User.UserRole.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
        System.out.println("✓ Admin created:");
        System.out.println("  Email: admin@amazonlite.com");
        System.out.println("  Password: admin123");
        System.out.println("  Role: ADMIN\n");

        // Create Seller User
        User seller = new User();
        seller.setEmail("seller@amazonlite.com");
        seller.setPasswordHash(passwordEncoder.encode("seller123"));  // Password: seller123
        seller.setRole(User.UserRole.SELLER);
        seller.setCreatedAt(LocalDateTime.now());
        userRepository.save(seller);
        System.out.println("✓ Seller created:");
        System.out.println("  Email: seller@amazonlite.com");
        System.out.println("  Password: seller123");
        System.out.println("  Role: SELLER\n");

        // Create Customer User
        User customer = new User();
        customer.setEmail("customer@amazonlite.com");
        customer.setPasswordHash(passwordEncoder.encode("customer123"));  // Password: customer123
        customer.setRole(User.UserRole.CUSTOMER);
        customer.setCreatedAt(LocalDateTime.now());
        userRepository.save(customer);
        System.out.println("✓ Customer created:");
        System.out.println("  Email: customer@amazonlite.com");
        System.out.println("  Password: customer123");
        System.out.println("  Role: CUSTOMER\n");

        System.out.println("==================== Test Data Initialized ====================\n");
    }
}

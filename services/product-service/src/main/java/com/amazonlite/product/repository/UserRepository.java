package com.amazonlite.product.repository;

import com.amazonlite.product.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for User entity.
 * Provides database operations for User records.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by email.
     * @param email the user's email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
}

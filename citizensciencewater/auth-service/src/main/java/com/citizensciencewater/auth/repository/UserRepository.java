package com.citizensciencewater.auth.repository;


import com.citizensciencewater.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom query methods for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by username.
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by email.
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find a user by username or email.
     * @param username the username to search for
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    /**
     * Check if a username already exists.
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if an email already exists.
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all users by role.
     * @param role the role to filter by (e.g., "CITIZEN", "ADMIN")
     * @return List of users with the specified role
     */
    List<User> findByRole(String role);
    
    /**
     * Find all active users.
     * @return List of active users
     */
    List<User> findByIsActiveTrue();
    
    /**
     * Find all inactive users.
     * @return List of inactive users
     */
    List<User> findByIsActiveFalse();
    
    /**
     * Find active users by role.
     * @param role the role to filter by
     * @return List of active users with the specified role
     */
    List<User> findByRoleAndIsActiveTrue(String role);
    
    /**
     * Search users by username or full name containing the search term (case-insensitive).
     * @param searchTerm the term to search for
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByUsernameOrFullName(@Param("searchTerm") String searchTerm);
    
    /**
     * Count users by role.
     * @param role the role to count
     * @return number of users with the specified role
     */
    long countByRole(String role);
    
    /**
     * Count active users.
     * @return number of active users
     */
    long countByIsActiveTrue();
    
    /**
     * Find users by email domain.
     * @param domain the email domain (e.g., "gmail.com")
     * @return List of users with emails from the specified domain
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%@', :domain)")
    List<User> findByEmailDomain(@Param("domain") String domain);
}
package com.citizensciencewater.auth.service;

import com.citizensciencewater.auth.dto.AuthResponse;
import com.citizensciencewater.auth.dto.*;
import com.citizensciencewater.auth.models.User;
import com.citizensciencewater.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service class for handling authentication operations.
 * Manages user registration, login, and token operations.
 */
@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    /**
     * Register a new user.
     * * @param registerRequest the registration details
     * @return AuthResponse with JWT token and user information
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        // Validate username availability
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        // Validate email availability
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        
        // 1. Create new user entity with basic info
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole("CITIZEN"); // Default role
        user.setActive(true);
        
        // 2. Initial Save: Persist user to get the auto-generated 'id'.
        // The @PrePersist sets the timestamps here.
        user = userRepository.save(user);
        
        // 3. Generate Citizen ID: Use the auto-generated 'id' to create the unique ID.
        // Format: C-001, C-010, C-123
        String citizenId = String.format("C-%03d", user.getId());
        user.setCitizenId(citizenId);
        
        // 4. Second Save: Persist the user again to save the new citizenId value.
        user = userRepository.save(user);

        logger.info("New user registered: {} with Citizen ID: {}", user.getUsername(), user.getCitizenId());
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getId());
        
        // Build response, now including citizenId
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole(), user.getCitizenId());
    }
    
    /**
     * Authenticate user and generate JWT token.
     * * @param loginRequest the login credentials
     * @return AuthResponse with JWT token and user information
     * @throws IllegalArgumentException if credentials are invalid
     */
    public AuthResponse login(LoginRequest loginRequest) {
        // Find user by username or email
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .or(() -> userRepository.findByEmail(loginRequest.getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        
        // Check if user is active
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated. Please contact support.");
        }
        
        // Validate password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        logger.info("User logged in: {}", user.getUsername());
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getUsername(), user.getRole(), user.getId());
        
        // Build response, now including citizenId
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole(), user.getCitizenId());
    }
    
    
    /**
     * Refreshes the JWT token using an existing (refresh) token.
     * * @param refreshToken the existing JWT token to refresh
     * @return AuthResponse with a new JWT token and user information
     * @throws IllegalArgumentException if the token is invalid or user not found
     */
    public AuthResponse refreshToken(String refreshToken) {
        // 1. Validate the refresh token
        if (!jwtService.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        
        // 2. Extract user identifier
        String username = jwtService.extractUsername(refreshToken);
        
        // 3. Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for token"));
        
        // 4. Generate a new JWT token
        String newToken = jwtService.generateToken(user.getUsername(), user.getRole(), user.getId());
        
        logger.info("Token successfully refreshed for user: {}", user.getUsername());
        
        // 5. Build response, now including citizenId
        return new AuthResponse(newToken, user.getUsername(), user.getEmail(), user.getRole(), user.getCitizenId());
    }
    
    /**
     * Validate JWT token.
     * * @param token the JWT token
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }
    
    /**
     * Get user information from JWT token.
     * * @param token the JWT token
     * @return Map containing user information
     * @throws IllegalArgumentException if token is invalid
     */
    public Map<String, Object> getUserInfoFromToken(String token) {
        if (!jwtService.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);
        Long userId = jwtService.extractUserId(token);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("citizenId", user.getCitizenId()); // Citizen ID included here
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("role", user.getRole());
        userInfo.put("isActive", user.isActive());
        userInfo.put("tokenValidity", jwtService.getRemainingValidity(token));
        
        return userInfo;
    }
    
    /**
     * Check if username is available.
     * * @param username the username to check
     * @return true if available, false otherwise
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    /**
     * Check if email is available.
     * * @param email the email to check
     * @return true if available, false otherwise
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}

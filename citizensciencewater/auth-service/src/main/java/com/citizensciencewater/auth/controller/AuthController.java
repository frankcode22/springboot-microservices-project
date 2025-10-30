package com.citizensciencewater.auth.controller;



import com.citizensciencewater.auth.dto.*;
import com.citizensciencewater.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
/**
 * REST controller for authentication endpoints.
 * Handles user registration, login, and token refresh operations.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")

public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Register a new user.
     * 
     * @param registerRequest the registration details
     * @return ResponseEntity with authentication response containing user details and JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            logger.info("Registration attempt for username: {}", registerRequest.getUsername());
            
            AuthResponse response = authService.register(registerRequest);
            
            logger.info("User registered successfully: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Registration error for username: {}", registerRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Registration failed. Please try again later."));
        }
    }
    
    /**
     * Authenticate a user and return JWT token.
     * 
     * @param loginRequest the login credentials
     * @return ResponseEntity with authentication response containing JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for user: {}", loginRequest.getUsername());
            
            AuthResponse response = authService.login(loginRequest);
            
            logger.info("User logged in successfully: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid username or password"));
        } catch (Exception e) {
            logger.error("Login error for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Login failed. Please try again later."));
        }
    }
    
    /**
     * Refresh JWT token using refresh token.
     * 
     * @param refreshTokenRequest the refresh token
     * @return ResponseEntity with new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        try {
            String refreshToken = refreshTokenRequest.get("refreshToken");
            
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Refresh token is required"));
            }
            
            logger.info("Token refresh attempt");
            
            AuthResponse response = authService.refreshToken(refreshToken);
            
            logger.info("Token refreshed successfully");
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid or expired refresh token"));
        } catch (Exception e) {
            logger.error("Token refresh error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Token refresh failed. Please try again later."));
        }
    }
    
    /**
     * Validate JWT token.
     * 
     * @param tokenRequest the token to validate
     * @return ResponseEntity with validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String token = tokenRequest.get("token");
            
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Token is required"));
            }
            
            boolean isValid = authService.validateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                response.put("message", "Token is valid");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Token is invalid or expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Token validation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Token validation failed"));
        }
    }
    
    /**
     * Logout user (client-side token removal, optional server-side token blacklisting).
     * 
     * @return ResponseEntity with logout confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            logger.info("Logout attempt");
            
            // Optional: Implement token blacklisting here if needed
            // authService.blacklistToken(token);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Logout failed"));
        }
    }
    
    /**
     * Get current user information from JWT token.
     * 
     * @param token the JWT token from Authorization header
     * @return ResponseEntity with user information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            Map<String, Object> userInfo = authService.getUserInfoFromToken(token);
            
            return ResponseEntity.ok(userInfo);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Get current user failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid or expired token"));
        } catch (Exception e) {
            logger.error("Get current user error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve user information"));
        }
    }
    
    /**
     * Check if username is available.
     * 
     * @param username the username to check
     * @return ResponseEntity with availability status
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        try {
            boolean available = authService.isUsernameAvailable(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("available", available);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Check username error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to check username availability"));
        }
    }
    
    /**
     * Check if email is available.
     * 
     * @param email the email to check
     * @return ResponseEntity with availability status
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        try {
            boolean available = authService.isEmailAvailable(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("email", email);
            response.put("available", available);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Check email error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to check email availability"));
        }
    }
    
    /**
     * Health check endpoint.
     * 
     * @return ResponseEntity with service status
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Creates a standardized error response.
     * 
     * @param message the error message
     * @return Map containing error details
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return error;
    }
}
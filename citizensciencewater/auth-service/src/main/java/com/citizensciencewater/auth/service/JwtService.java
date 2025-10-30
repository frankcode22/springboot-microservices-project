package com.citizensciencewater.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for handling JWT token operations including generation, validation, and claims extraction.
 * Uses HMAC-SHA algorithms for token signing.
 */
@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_USER_ID = "userId";
    private static final long MILLISECONDS_PER_SECOND = 1000L;
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:86400000}") // Default: 24 hours in milliseconds
    private Long jwtExpiration;
    
    @Value("${jwt.refresh-expiration:604800000}") // Default: 7 days in milliseconds
    private Long refreshExpiration;
    
    /**
     * Generates a JWT token for the given username and role.
     *
     * @param username the username
     * @param role the user's role
     * @return the generated JWT token
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ROLE, role);
        return generateToken(claims, username);
    }
    
    /**
     * Generates a JWT token for the given username, role, and user ID.
     *
     * @param username the username
     * @param role the user's role
     * @param userId the user's ID
     * @return the generated JWT token
     */
    public String generateToken(String username, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ROLE, role);
        claims.put(CLAIM_USER_ID, userId);
        return generateToken(claims, username);
    }
    
    /**
     * Generates a JWT token with custom claims.
     *
     * @param extraClaims additional claims to include in the token
     * @param username the subject (username)
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return buildToken(extraClaims, username, jwtExpiration);
    }
    
    /**
     * Generates a refresh token for the given username.
     *
     * @param username the username
     * @return the generated refresh token
     */
    public String generateRefreshToken(String username) {
        return buildToken(new HashMap<>(), username, refreshExpiration);
    }
    
    /**
     * Builds a JWT token with specified claims, subject, and expiration.
     *
     * @param extraClaims additional claims
     * @param subject the subject (username)
     * @param expiration expiration time in milliseconds
     * @return the JWT token
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            Long expiration
    ) {
        long currentTimeMillis = System.currentTimeMillis();
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(currentTimeMillis))
                .expiration(new Date(currentTimeMillis + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
    
    /**
     * Validates the JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("JWT token is malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.error("JWT signature validation failed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT token is empty or null: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT token validation failed: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Checks if the token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            logger.error("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Extracts the username (subject) from the token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extracts the role from the token.
     *
     * @param token the JWT token
     * @return the role
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get(CLAIM_ROLE, String.class));
    }
    
    /**
     * Extracts the user ID from the token.
     *
     * @param token the JWT token
     * @return the user ID, or null if not present
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> {
            Object userId = claims.get(CLAIM_USER_ID);
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
            return null;
        });
    }
    
    /**
     * Extracts the expiration date from the token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extracts the issued-at date from the token.
     *
     * @param token the JWT token
     * @return the issued-at date
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }
    
    /**
     * Extracts all claims from the token.
     *
     * @param token the JWT token
     * @return the claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Extracts a specific claim from the token using a claims resolver function.
     *
     * @param token the JWT token
     * @param claimsResolver function to extract the claim
     * @param <T> the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Gets the remaining validity time of the token in seconds.
     *
     * @param token the JWT token
     * @return remaining time in seconds, or 0 if expired
     */
    public long getRemainingValidity(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remainingMillis = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remainingMillis / MILLISECONDS_PER_SECOND);
        } catch (JwtException e) {
            logger.error("Failed to get remaining validity: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Checks if the token can be refreshed (not expired beyond refresh period).
     *
     * @param token the JWT token
     * @return true if the token can be refreshed
     */
    public boolean canTokenBeRefreshed(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date issuedAt = extractIssuedAt(token);
            long tokenAge = System.currentTimeMillis() - issuedAt.getTime();
            
            // Allow refresh if token is within refresh period
            return tokenAge < refreshExpiration;
        } catch (JwtException e) {
            logger.error("Failed to check if token can be refreshed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the signing key for JWT operations.
     *
     * @return the secret key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Validates the token format without checking expiration.
     * Useful for refresh token scenarios.
     *
     * @param token the JWT token
     * @return true if the token format is valid
     */
    public boolean isTokenFormatValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            logger.error("Invalid token format: {}", e.getMessage());
            return false;
        }
    }
}
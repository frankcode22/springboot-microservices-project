package com.citizensciencewater.auth.config;

import com.citizensciencewater.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * JWT Authentication Filter that intercepts requests and validates JWT tokens.
 * Skips public endpoints (like /login, /register, /refresh).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    // Exact paths that skip JWT validation
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/",
            "/actuator/health"
    );

    // Path prefixes (e.g., swagger docs, public APIs)
    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
            "/api/public/",
            "/swagger-ui",
            "/v3/api-docs"
    );

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && jwtService.validateToken(jwt)) {
                authenticateUser(jwt, request);
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed", e);
            // Clear authentication on error
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from the Authorization header.
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX_LENGTH);
        }

        return null;
    }

    /**
     * Authenticates the user and sets SecurityContext.
     */
    private void authenticateUser(String jwt, HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return; // already authenticated
        }

        String username = jwtService.extractUsername(jwt);
        String role = jwtService.extractRole(jwt);

        if (!StringUtils.hasText(username) || !StringUtils.hasText(role)) {
            logger.warn("Invalid JWT token: missing username or role");
            return;
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(authority)
        );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        logger.debug("User '{}' authenticated with role '{}'", username, role);
    }

    /**
     * Determines whether to skip JWT filtering for public endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Skip if path exactly matches a public one
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }

        // Skip if path starts with known public prefixes
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }
}

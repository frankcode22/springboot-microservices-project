package com.citizensciencewater.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Gateway Controller.
 * Acts as a unified entry point for all microservices.
 * Routes requests to appropriate microservices and returns responses.
 */
@RestController
@RequestMapping("/gateway")
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = "http://localhost:5173")
public class GatewayController {
    
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);
    
    // Microservice URLs
    @Value("${crowdsourced.data.service.url}")
    private String crowdsourcedDataServiceUrl;
    
    @Value("${rewards.service.url}")
    private String rewardsServiceUrl;

    @Value("${auth.service.url}")
    private String authServiceUrl; // Added Auth Service URL
    
    private final RestTemplate restTemplate;
    
    /**
     * Constructor initializing RestTemplate
     */
    public GatewayController() {
        this.restTemplate = new RestTemplate();
    }

    // =================================================================================
    // COMMON ROUTING HELPERS
    // =================================================================================

    /**
     * Common method to route requests to the Rewards Service.
     */
    private ResponseEntity<?> routeToRewardsService(HttpMethod method, String path, @RequestBody(required = false) Object requestBody) {
        String url = rewardsServiceUrl + path;
        log.info("Gateway: Routing {} request to Rewards Service at: {}", method, url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                method,
                requestEntity,
                Object.class
            );
            
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            
        } catch (HttpClientErrorException e) {
            log.error("Error from Rewards Service ({} {}): {}", method, url, e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Gateway error while routing to Rewards Service: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Common method to route requests to the Auth Service, including proxying headers.
     */
    private ResponseEntity<?> routeToAuthService(HttpMethod method, String path, @RequestBody(required = false) Object requestBody, @RequestHeader HttpHeaders incomingHeaders) {
        String url = authServiceUrl + path;
        log.info("Gateway: Routing {} request to Auth Service at: {}", method, url);

        try {
            // Forward relevant headers (like Authorization)
            HttpHeaders outgoingHeaders = new HttpHeaders();
            outgoingHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (incomingHeaders.containsKey("authorization")) {
                outgoingHeaders.set("Authorization", incomingHeaders.getFirst("authorization"));
            }

            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, outgoingHeaders);

            ResponseEntity<Object> response = restTemplate.exchange(
                url,
                method,
                requestEntity,
                Object.class
            );
            
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            
        } catch (HttpClientErrorException e) {
            log.error("Error from Auth Service ({} {}): {}", method, url, e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Gateway error while routing to Auth Service: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // =================================================================================
    // CROWDSOURCED DATA ENDPOINTS
    // =================================================================================

    @PostMapping("/observations")
    public ResponseEntity<?> submitObservation(@RequestBody Map<String, Object> observationData) {
        log.info("Gateway: Routing observation submission to Crowdsourced Data Service");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(observationData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                crowdsourcedDataServiceUrl,
                request,
                Map.class
            );
            
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            
        } catch (HttpClientErrorException e) {
            log.error("Error from Crowdsourced Data Service: {}", e.getMessage(), e);
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Gateway error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/observations")
    public ResponseEntity<?> getAllObservations() {
        log.info("Gateway: Routing request to get all observations");
        
        try {
            ResponseEntity<?> response = restTemplate.getForEntity(
                crowdsourcedDataServiceUrl,
                Object.class
            );
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Gateway error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/observations/citizen/{citizenId}")
    public ResponseEntity<?> getObservationsByCitizen(@PathVariable String citizenId) {
        log.info("Gateway: Routing request to get observations for citizen: {}", citizenId);
        
        try {
            String url = crowdsourcedDataServiceUrl + "/citizen/" + citizenId;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Gateway error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/observations/recent")
    public ResponseEntity<?> getRecentObservations() {
        log.info("Gateway: Routing request to get recent observations");
        
        try {
            String url = crowdsourcedDataServiceUrl + "/recent";
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Gateway error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =================================================================================
    // REWARDS SERVICE ENDPOINTS
    // =================================================================================

    // --- CRUD Endpoints ---
    @GetMapping("/rewards/citizen/{citizenId}")
    public ResponseEntity<?> getCitizenReward(@PathVariable String citizenId) {
        return routeToRewardsService(HttpMethod.GET, "/citizen/" + citizenId, null);
    }
    @GetMapping("/rewards/all")
    public ResponseEntity<?> getAllCitizenRewards() {
        return routeToRewardsService(HttpMethod.GET, "/all", null);
    }
    @PostMapping("/rewards/citizen")
    public ResponseEntity<?> createCitizenReward(@RequestBody Map<String, String> request) {
        return routeToRewardsService(HttpMethod.POST, "/citizen", request);
    }
    @DeleteMapping("/rewards/citizen/{citizenId}")
    public ResponseEntity<?> deleteCitizenReward(@PathVariable String citizenId) {
        return routeToRewardsService(HttpMethod.DELETE, "/citizen/" + citizenId, null);
    }

    // --- Observation Processing Endpoints ---
    @PostMapping("/rewards/citizen/{citizenId}/observation")
    public ResponseEntity<?> addObservation(
            @PathVariable String citizenId,
            @RequestBody Map<String, Boolean> request) {
        return routeToRewardsService(HttpMethod.POST, "/citizen/" + citizenId + "/observation", request);
    }
    @PostMapping("/rewards/citizen/{citizenId}/observations/batch")
    public ResponseEntity<?> addMultipleObservations(
            @PathVariable String citizenId,
            @RequestBody Map<String, Integer> request) {
        return routeToRewardsService(HttpMethod.POST, "/citizen/" + citizenId + "/observations/batch", request);
    }

    // --- Leaderboard Endpoints ---
    @GetMapping("/rewards/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        String path = UriComponentsBuilder.fromPath("/leaderboard")
                .queryParam("limit", limit)
                .toUriString();
        return routeToRewardsService(HttpMethod.GET, path, null);
    }
    @GetMapping("/rewards/leaderboard/full")
    public ResponseEntity<?> getFullLeaderboard() {
        return routeToRewardsService(HttpMethod.GET, "/leaderboard/full", null);
    }
    @GetMapping("/rewards/citizen/{citizenId}/rank")
    public ResponseEntity<?> getCitizenRank(@PathVariable String citizenId) {
        return routeToRewardsService(HttpMethod.GET, "/citizen/" + citizenId + "/rank", null);
    }

    // --- Badge & Filter Endpoints ---
    @GetMapping("/rewards/badge/{badgeLevel}")
    public ResponseEntity<?> getCitizensByBadge(@PathVariable String badgeLevel) {
        return routeToRewardsService(HttpMethod.GET, "/badge/" + badgeLevel, null);
    }
    @GetMapping("/rewards/filter/points")
    public ResponseEntity<?> getCitizensByMinimumPoints(
            @RequestParam int minPoints) {
        String path = UriComponentsBuilder.fromPath("/filter/points")
                .queryParam("minPoints", minPoints)
                .toUriString();
        return routeToRewardsService(HttpMethod.GET, path, null);
    }
    @GetMapping("/rewards/filter/observations")
    public ResponseEntity<?> getCitizensByMinimumObservations(
            @RequestParam int minObservations) {
        String path = UriComponentsBuilder.fromPath("/filter/observations")
                .queryParam("minObservations", minObservations)
                .toUriString();
        return routeToRewardsService(HttpMethod.GET, path, null);
    }
    
    // --- Statistics Endpoints ---
    @GetMapping("/rewards/statistics")
    public ResponseEntity<?> getStatistics() {
        return routeToRewardsService(HttpMethod.GET, "/statistics", null);
    }
    @GetMapping("/rewards/statistics/badges")
    public ResponseEntity<?> getBadgeDistribution() {
        return routeToRewardsService(HttpMethod.GET, "/statistics/badges", null);
    }
    
    // --- Admin Endpoints ---
    @PostMapping("/rewards/citizen/{citizenId}/reward")
    public ResponseEntity<?> rewardCitizen(
            @PathVariable String citizenId,
            @RequestBody Map<String, Object> request) {
        return routeToRewardsService(HttpMethod.POST, "/citizen/" + citizenId + "/reward", request);
    }
    @PostMapping("/rewards/citizen/{citizenId}/points")
    public ResponseEntity<?> addPoints(
            @PathVariable String citizenId,
            @RequestBody Map<String, Integer> request) {
        return routeToRewardsService(HttpMethod.POST, "/citizen/" + citizenId + "/points", request);
    }
    @PostMapping("/rewards/citizen/{citizenId}/reset")
    public ResponseEntity<?> resetCitizenReward(@PathVariable String citizenId) {
        return routeToRewardsService(HttpMethod.POST, "/citizen/" + citizenId + "/reset", null);
    }


    // =================================================================================
    // AUTH SERVICE ENDPOINTS (New)
    // =================================================================================

    /**
     * Routes registration request.
     * Maps POST /gateway/auth/register to POST /api/auth/register
     */
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Object requestBody) {
        return routeToAuthService(HttpMethod.POST, "/register", requestBody, new HttpHeaders());
    }

    /**
     * Routes login request.
     * Maps POST /gateway/auth/login to POST /api/auth/login
     */
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Object requestBody) {
        return routeToAuthService(HttpMethod.POST, "/login", requestBody, new HttpHeaders());
    }

    /**
     * Routes token refresh request.
     * Maps POST /gateway/auth/refresh to POST /api/auth/refresh
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        return routeToAuthService(HttpMethod.POST, "/refresh", refreshTokenRequest, new HttpHeaders());
    }

    /**
     * Routes token validation request.
     * Maps POST /gateway/auth/validate to POST /api/auth/validate
     */
    @PostMapping("/auth/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
        return routeToAuthService(HttpMethod.POST, "/validate", tokenRequest, new HttpHeaders());
    }

    /**
     * Routes logout request (proxies Authorization header).
     * Maps POST /gateway/auth/logout to POST /api/auth/logout
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestHeader HttpHeaders headers) {
        return routeToAuthService(HttpMethod.POST, "/logout", null, headers);
    }

    /**
     * Routes "Get Current User" request (proxies Authorization header).
     * Maps GET /gateway/auth/me to GET /api/auth/me
     */
    @GetMapping("/auth/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader HttpHeaders headers) {
        return routeToAuthService(HttpMethod.GET, "/me", null, headers);
    }

    /**
     * Routes check username availability request.
     * Maps GET /gateway/auth/check-username to GET /api/auth/check-username
     */
    @GetMapping("/auth/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        String path = UriComponentsBuilder.fromPath("/check-username")
                .queryParam("username", username)
                .toUriString();
        return routeToAuthService(HttpMethod.GET, path, null, new HttpHeaders());
    }

    /**
     * Routes check email availability request.
     * Maps GET /gateway/auth/check-email to GET /api/auth/check-email
     */
    @GetMapping("/auth/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        String path = UriComponentsBuilder.fromPath("/check-email")
                .queryParam("email", email)
                .toUriString();
        return routeToAuthService(HttpMethod.GET, path, null, new HttpHeaders());
    }

    /**
     * Routes Auth health check request.
     * Maps GET /gateway/auth/health to GET /api/auth/health
     */
    @GetMapping("/auth/health")
    public ResponseEntity<?> authHealthCheck() {
        return routeToAuthService(HttpMethod.GET, "/health", null, new HttpHeaders());
    }


    // =================================================================================
    // HEALTH CHECK (Updated to include Auth Service)
    // =================================================================================
    
    /**
     * Health check endpoint
     * Returns status of API Gateway and connected microservices
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Gateway: Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("gateway", "UP");
        
        // 1. Check Crowdsourced Data Service
        try {
            restTemplate.getForEntity(crowdsourcedDataServiceUrl + "/health", Object.class);
            health.put("crowdsourcedDataService", "UP");
            log.debug("Crowdsourced Data Service is UP");
        } catch (Exception e) {
            health.put("crowdsourcedDataService", "DOWN");
            log.warn("Crowdsourced Data Service is DOWN: {}", e.getMessage());
        }
        
        // 2. Check Rewards Service
        try {
            restTemplate.getForEntity(rewardsServiceUrl + "/health", Object.class);
            health.put("rewardsService", "UP");
            log.debug("Rewards Service is UP");
        } catch (Exception e) {
            health.put("rewardsService", "DOWN");
            log.warn("Rewards Service is DOWN: {}", e.getMessage());
        }

        // 3. Check Auth Service (New)
        try {
            restTemplate.getForEntity(authServiceUrl + "/health", Object.class);
            health.put("authService", "UP");
            log.debug("Auth Service is UP");
        } catch (Exception e) {
            health.put("authService", "DOWN");
            log.warn("Auth Service is DOWN: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }
}

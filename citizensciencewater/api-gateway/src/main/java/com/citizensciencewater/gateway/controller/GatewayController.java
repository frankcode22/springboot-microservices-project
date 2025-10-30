package com.citizensciencewater.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * API Gateway Controller.
 * Acts as a unified entry point for all microservices.
 * Routes requests to appropriate microservices and returns responses.
 */
@RestController
@RequestMapping("/gateway")
@CrossOrigin(origins = "*")
public class GatewayController {
    
    private static final Logger log = LoggerFactory.getLogger(GatewayController.class);
    
    @Value("${crowdsourced.data.service.url}")
    private String crowdsourcedDataServiceUrl;
    
    @Value("${rewards.service.url}")
    private String rewardsServiceUrl;
    
    private final RestTemplate restTemplate;
    
    /**
     * Constructor initializing RestTemplate
     */
    public GatewayController() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Gateway endpoint for submitting observations
     * Routes to Crowdsourced Data Service
     */
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
    
    /**
     * Gateway endpoint for retrieving all observations
     * Routes to Crowdsourced Data Service
     */
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
    
    /**
     * Gateway endpoint for retrieving observations by citizen
     * Routes to Crowdsourced Data Service
     */
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
    
    /**
     * Gateway endpoint for retrieving recent observations
     * Routes to Crowdsourced Data Service
     */
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
    
    /**
     * Gateway endpoint for retrieving citizen rewards
     * Routes to Rewards Service
     */
    @GetMapping("/rewards/citizen/{citizenId}")
    public ResponseEntity<?> getRewardsForCitizen(@PathVariable String citizenId) {
        log.info("Gateway: Routing request to get rewards for citizen: {}", citizenId);
        
        try {
            String url = rewardsServiceUrl + "/citizen/" + citizenId;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Gateway error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Gateway endpoint for calculating citizen rewards
     * Routes to Rewards Service
     */
    @PostMapping("/rewards/citizen/{citizenId}/calculate")
    public ResponseEntity<?> calculateRewardsForCitizen(@PathVariable String citizenId) {
        log.info("Gateway: Routing request to calculate rewards for citizen: {}", citizenId);
        
        try {
            String url = rewardsServiceUrl + "/citizen/" + citizenId + "/calculate";
            ResponseEntity<?> response = restTemplate.postForEntity(url, null, Object.class);
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Gateway error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Gateway endpoint for retrieving leaderboard
     * Routes to Rewards Service
     */
    @GetMapping("/rewards/leaderboard")
    public ResponseEntity<?> getLeaderboard(@RequestParam(defaultValue = "10") int topN) {
        log.info("Gateway: Routing request to get leaderboard");
        
        try {
            String url = rewardsServiceUrl + "/leaderboard?topN=" + topN;
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Gateway error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Gateway endpoint for retrieving top 3 leaderboard
     * Routes to Rewards Service
     */
    @GetMapping("/rewards/leaderboard/top3")
    public ResponseEntity<?> getTopThreeLeaderboard() {
        log.info("Gateway: Routing request to get top 3 leaderboard");
        
        try {
            String url = rewardsServiceUrl + "/leaderboard/top3";
            ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Gateway error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Gateway error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Health check endpoint
     * Returns status of API Gateway and connected microservices
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Gateway: Health check requested");
        
        Map<String, Object> health = new HashMap<>();
        health.put("gateway", "UP");
        
        // Check Crowdsourced Data Service
        try {
            restTemplate.getForEntity(crowdsourcedDataServiceUrl, Object.class);
            health.put("crowdsourcedDataService", "UP");
            log.debug("Crowdsourced Data Service is UP");
        } catch (Exception e) {
            health.put("crowdsourcedDataService", "DOWN");
            log.warn("Crowdsourced Data Service is DOWN: {}", e.getMessage());
        }
        
        // Check Rewards Service
        try {
            restTemplate.getForEntity(rewardsServiceUrl + "/statistics", Object.class);
            health.put("rewardsService", "UP");
            log.debug("Rewards Service is UP");
        } catch (Exception e) {
            health.put("rewardsService", "DOWN");
            log.warn("Rewards Service is DOWN: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }
}
package com.citizensciencewater.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for the Rewards Service.
 * This microservice calculates and manages citizen rewards based on
 * their water quality observation contributions.
 * 
 * The service:
 * - Reads observation data from the Crowdsourced Data Service
 * - Calculates points (10 points per valid observation, +10 bonus for complete)
 * - Awards achievement badges (Bronze at 100, Silver at 200, Gold at 500 points)
 * - Maintains leaderboard of top contributors
 * - Stores reward data in-memory (not persisted)
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
@EnableJpaAuditing
public class RewardsServiceApplication {
    
    /**
     * Main method to launch the Spring Boot application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(RewardsServiceApplication.class, args);
        
        System.out.println("\n===========================================");
        System.out.println("   Rewards Service Started Successfully!   ");
        System.out.println("===========================================");
        System.out.println("Server running on: http://localhost:8082");
        System.out.println("API Base URL: http://localhost:8082/api/rewards");
        System.out.println("");
        System.out.println("Available Endpoints:");
        System.out.println("  GET  /api/rewards/citizen/{citizenId}");
        System.out.println("  POST /api/rewards/citizen/{citizenId}/calculate");
        System.out.println("  GET  /api/rewards/leaderboard");
        System.out.println("  GET  /api/rewards/leaderboard/top3");
        System.out.println("  POST /api/rewards/refresh");
        System.out.println("  GET  /api/rewards/statistics");
        System.out.println("===========================================\n");
    }
    
    /**
     * Bean configuration for RestTemplate.
     * Used for making HTTP requests to other microservices.
     * RestTemplate allows this service to communicate with the
     * Crowdsourced Data Service to fetch observation data.
     * 
     * @return RestTemplate instance for inter-service communication
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
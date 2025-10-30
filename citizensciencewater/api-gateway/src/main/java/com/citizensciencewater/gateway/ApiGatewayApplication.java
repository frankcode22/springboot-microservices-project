//package com.citizensciencewater.gateway;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class ApiGatewayApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(ApiGatewayApplication.class, args);
//	}
//
//}


package com.citizensciencewater.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Main application class for the API Gateway.
 * This service acts as a unified entry point for all microservices,
 * routing requests to the appropriate backend services.
 * 
 * The gateway:
 * - Routes requests to Crowdsourced Data Service (port 8081)
 * - Routes requests to Rewards Service (port 8082)
 * - Provides a single endpoint for client applications
 * - Handles error responses and service communication
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
public class ApiGatewayApplication {
    
    /**
     * Main method to launch the Spring Boot application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("API Gateway Started Successfully!");
        System.out.println("Server running on: http://localhost:8080");
        System.out.println("Gateway Base URL: http://localhost:8080/gateway");
        System.out.println("Health Check: http://localhost:8080/gateway/health");
        System.out.println("===========================================\n");
    }
    
    /**
     * Bean configuration for RestTemplate
     * Used for making HTTP requests to microservices
     * 
     * @return RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

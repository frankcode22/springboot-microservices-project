//package com.citizensciencewater;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class CitizensciencewaterApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(CitizensciencewaterApplication.class, args);
//	}
//
//}
//
//
//

package com.citizensciencewater;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Citizen Science Water Quality Monitoring System.
 * This class bootstraps the Spring Boot application and initializes all components.
 * 
 * The application provides a microservices-based platform for:
 * - Collecting crowdsourced water quality observations from citizens
 * - Validating and storing water quality data
 * - Managing citizen contributions and rewards
 * 
 * @author Your Name
 * @version 1.0
 * @since 2025
 */
@SpringBootApplication
public class CitizensciencewaterApplication {
    
    /**
     * Main method to launch the Spring Boot application
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CitizensciencewaterApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("Crowdsourced Data Service Started Successfully!");
        System.out.println("Server running on: http://localhost:8081");
        System.out.println("API Base URL: http://localhost:8081/api/observations");
        System.out.println("===========================================\n");
    }
}

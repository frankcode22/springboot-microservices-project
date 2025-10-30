package com.citizensciencewater.controller;

import com.citizensciencewater.models.Observation;
import com.citizensciencewater.models.ObservationDTO;
import com.citizensciencewater.services.ObservationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for handling water quality observation requests.
 * Provides endpoints for submitting, retrieving, and managing observations.
 */
@RestController
@RequestMapping("/api/observations")
@CrossOrigin(origins = "*")
public class ObservationController {
    
    private static final Logger log = LoggerFactory.getLogger(ObservationController.class);
    
    private final ObservationService observationService;
    
    /**
     * Constructor with dependency injection
     * @param observationService the service handling observation business logic
     */
    @Autowired
    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }
    
    /**
     * POST endpoint to submit a new water quality observation
     * 
     * @param observationDTO the observation data from the request body
     * @return ResponseEntity with the saved observation and HTTP status
     * 
     * Example Request Body:
     * {
     *   "citizenId": "citizen123",
     *   "postcode": "NE1 8ST",
     *   "temperature": 15.5,
     *   "ph": 7.2,
     *   "alkalinity": 120.0,
     *   "turbidity": 5.0,
     *   "observations": ["Clear", "No Odour"],
     *   "imagePaths": ["image1.jpg", "image2.jpg"]
     * }
     */
    @PostMapping
    public ResponseEntity<?> submitObservation(@Valid @RequestBody ObservationDTO observationDTO) {
        try {
            log.info("POST /api/observations - Submitting observation");
            Observation savedObservation = observationService.submitObservation(observationDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Observation submitted successfully");
            response.put("observation", savedObservation);
            response.put("isComplete", savedObservation.isComplete());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid observation: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            log.error("Error submitting observation: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * GET endpoint to retrieve all observations
     * @return list of all observations
     */
    @GetMapping
    public ResponseEntity<List<Observation>> getAllObservations() {
        log.info("GET /api/observations - Fetching all observations");
        List<Observation> observations = observationService.getAllObservations();
        return ResponseEntity.ok(observations);
    }
    
    /**
     * GET endpoint to retrieve a specific observation by ID
     * @param id the observation ID
     * @return the observation if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getObservationById(@PathVariable String id) {
        log.info("GET /api/observations/{} - Fetching observation", id);
        Optional<Observation> observation = observationService.getObservationById(id);
        
        if (observation.isPresent()) {
            return ResponseEntity.ok(observation.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Observation not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    /**
     * GET endpoint to retrieve all observations for a specific citizen
     * @param citizenId the citizen's unique identifier
     * @return list of observations for the citizen
     */
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<List<Observation>> getObservationsByCitizen(@PathVariable String citizenId) {
        log.info("GET /api/observations/citizen/{} - Fetching observations", citizenId);
        List<Observation> observations = observationService.getObservationsByCitizen(citizenId);
        return ResponseEntity.ok(observations);
    }
    
    /**
     * GET endpoint to retrieve all valid observations for a specific citizen
     * @param citizenId the citizen's unique identifier
     * @return list of valid observations for the citizen
     */
    @GetMapping("/citizen/{citizenId}/valid")
    public ResponseEntity<List<Observation>> getValidObservationsByCitizen(@PathVariable String citizenId) {
        log.info("GET /api/observations/citizen/{}/valid - Fetching valid observations", citizenId);
        List<Observation> observations = observationService.getValidObservationsByCitizen(citizenId);
        return ResponseEntity.ok(observations);
    }
    
    /**
     * GET endpoint to retrieve observations by postcode
     * @param postcode the postcode to search for
     * @return list of observations for the postcode
     */
    @GetMapping("/postcode/{postcode}")
    public ResponseEntity<List<Observation>> getObservationsByPostcode(@PathVariable String postcode) {
        log.info("GET /api/observations/postcode/{} - Fetching observations", postcode);
        List<Observation> observations = observationService.getObservationsByPostcode(postcode);
        return ResponseEntity.ok(observations);
    }
    
    /**
     * GET endpoint to retrieve all valid observations
     * @return list of all valid observations
     */
    @GetMapping("/valid")
    public ResponseEntity<List<Observation>> getAllValidObservations() {
        log.info("GET /api/observations/valid - Fetching all valid observations");
        List<Observation> observations = observationService.getAllValidObservations();
        return ResponseEntity.ok(observations);
    }
    
    /**
     * GET endpoint to retrieve the 5 most recent valid observations
     * @return list of 5 most recent valid observations
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Observation>> getRecentObservations() {
        log.info("GET /api/observations/recent - Fetching recent observations");
        List<Observation> observations = observationService.getRecentObservations();
        return ResponseEntity.ok(observations);
    }
    
    /**
     * GET endpoint to count total observations for a citizen
     * @param citizenId the citizen's unique identifier
     * @return count of observations
     */
    @GetMapping("/citizen/{citizenId}/count")
    public ResponseEntity<Map<String, Long>> countObservationsByCitizen(@PathVariable String citizenId) {
        log.info("GET /api/observations/citizen/{}/count - Counting observations", citizenId);
        long count = observationService.countObservationsByCitizen(citizenId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE endpoint to remove an observation
     * @param id the observation ID
     * @return success message or error
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteObservation(@PathVariable String id) {
        try {
            log.info("DELETE /api/observations/{} - Deleting observation", id);
            observationService.deleteObservation(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Observation deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting observation: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete observation");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
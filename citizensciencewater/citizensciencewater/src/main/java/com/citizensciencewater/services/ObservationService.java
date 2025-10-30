package com.citizensciencewater.services;

import com.citizensciencewater.models.Observation;
import com.citizensciencewater.models.ObservationDTO;
import com.citizensciencewater.repository.ObservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing water quality observations.
 * Contains business logic for validating, storing, and retrieving observations.
 */
@Service
public class ObservationService {
    
    private static final Logger log = LoggerFactory.getLogger(ObservationService.class);
    
    private final ObservationRepository observationRepository;
    
    /**
     * Constructor with dependency injection
     * @param observationRepository the repository for observation data access
     */
    @Autowired
    public ObservationService(ObservationRepository observationRepository) {
        this.observationRepository = observationRepository;
    }
    
    /**
     * Submit a new water quality observation.
     * Validates the observation and stores it if valid.
     * 
     * @param observationDTO the observation data transfer object
     * @return the saved observation entity
     * @throws IllegalArgumentException if observation is invalid
     */
    @Transactional
    public Observation submitObservation(ObservationDTO observationDTO) {
        log.info("Submitting new observation for citizen: {}", observationDTO.getCitizenId());
        
        // Convert DTO to entity
        Observation observation = observationDTO.toEntity();
        
        // Validate the observation
        boolean isValid = observation.validate();
        observation.setValid(isValid);
        
        if (!isValid) {
            log.warn("Invalid observation submitted - missing required fields");
            throw new IllegalArgumentException(
                "Invalid observation: must contain postcode AND at least one measurement or observation"
            );
        }
        
        // Check if observation is complete
        boolean isComplete = observation.checkComplete();
        observation.setComplete(isComplete);
        
        // Set submission timestamp
        observation.setSubmissionTimestamp(LocalDateTime.now());
        
        // Save to database
        Observation savedObservation = observationRepository.save(observation);
        log.info("Observation saved successfully with ID: {}", savedObservation.getId());
        
        return savedObservation;
    }
    
    /**
     * Retrieve all observations
     * @return list of all observations
     */
    public List<Observation> getAllObservations() {
        log.info("Fetching all observations");
        return observationRepository.findAll();
    }
    
    /**
     * Retrieve a specific observation by ID
     * @param id the observation ID
     * @return optional containing the observation if found
     */
    public Optional<Observation> getObservationById(String id) {
        log.info("Fetching observation with ID: {}", id);
        return observationRepository.findById(id);
    }
    
    /**
     * Retrieve all observations for a specific citizen
     * @param citizenId the citizen's unique identifier
     * @return list of observations for the citizen
     */
    public List<Observation> getObservationsByCitizen(String citizenId) {
        log.info("Fetching observations for citizen: {}", citizenId);
        return observationRepository.findByCitizenId(citizenId);
    }
    
    /**
     * Retrieve all valid observations for a specific citizen
     * @param citizenId the citizen's unique identifier
     * @return list of valid observations for the citizen
     */
    public List<Observation> getValidObservationsByCitizen(String citizenId) {
        log.info("Fetching valid observations for citizen: {}", citizenId);
        return observationRepository.findByCitizenIdAndIsValid(citizenId, true);
    }
    
    /**
     * Retrieve observations by postcode
     * @param postcode the postcode to search for
     * @return list of observations for the postcode
     */
    public List<Observation> getObservationsByPostcode(String postcode) {
        log.info("Fetching observations for postcode: {}", postcode);
        return observationRepository.findByPostcode(postcode);
    }
    
    /**
     * Retrieve all valid observations
     * @return list of all valid observations
     */
    public List<Observation> getAllValidObservations() {
        log.info("Fetching all valid observations");
        return observationRepository.findByIsValid(true);
    }
    
    /**
     * Get the 5 most recent valid observations
     * @return list of 5 most recent valid observations
     */
    public List<Observation> getRecentObservations() {
        log.info("Fetching 5 most recent valid observations");
        return observationRepository.findTop5RecentValidObservations();
    }
    
    /**
     * Count total observations for a citizen
     * @param citizenId the citizen's unique identifier
     * @return count of observations
     */
    public long countObservationsByCitizen(String citizenId) {
        log.info("Counting observations for citizen: {}", citizenId);
        return observationRepository.countByCitizenId(citizenId);
    }
    
    /**
     * Count valid observations for a citizen
     * @param citizenId the citizen's unique identifier
     * @return count of valid observations
     */
    public long countValidObservationsByCitizen(String citizenId) {
        log.info("Counting valid observations for citizen: {}", citizenId);
        return observationRepository.countByCitizenIdAndIsValid(citizenId, true);
    }
    
    /**
     * Delete an observation by ID
     * @param id the observation ID
     */
    @Transactional
    public void deleteObservation(String id) {
        log.info("Deleting observation with ID: {}", id);
        observationRepository.deleteById(id);
    }
}
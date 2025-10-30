package com.citizensciencewater.repository;

import com.citizensciencewater.models.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Observation entity.
 * Provides CRUD operations and custom queries for water quality observations.
 * Spring Data JPA automatically implements this interface at runtime.
 */
@Repository
public interface ObservationRepository extends JpaRepository<Observation, String> {
    
    /**
     * Find all observations submitted by a specific citizen
     * @param citizenId the unique identifier of the citizen
     * @return list of observations for the citizen
     */
    List<Observation> findByCitizenId(String citizenId);
    
    /**
     * Find all valid observations submitted by a specific citizen
     * @param citizenId the unique identifier of the citizen
     * @param isValid whether the observation is valid
     * @return list of valid observations for the citizen
     */
    List<Observation> findByCitizenIdAndIsValid(String citizenId, boolean isValid);
    
    /**
     * Find observations by postcode
     * @param postcode the postcode to search for
     * @return list of observations for the postcode
     */
    List<Observation> findByPostcode(String postcode);
    
    /**
     * Find all valid observations
     * @param isValid whether the observation is valid
     * @return list of valid observations
     */
    List<Observation> findByIsValid(boolean isValid);
    
    /**
     * Count total observations submitted by a citizen
     * @param citizenId the unique identifier of the citizen
     * @return count of observations
     */
    long countByCitizenId(String citizenId);
    
    /**
     * Count valid observations submitted by a citizen
     * @param citizenId the unique identifier of the citizen
     * @param isValid whether the observation is valid
     * @return count of valid observations
     */
    long countByCitizenIdAndIsValid(String citizenId, boolean isValid);
    
    /**
     * Find the most recent observations
     * @param isValid whether the observation is valid
     * @return list of recent observations ordered by submission time (descending)
     */
    @Query("SELECT o FROM Observation o WHERE o.isValid = :isValid ORDER BY o.submissionTimestamp DESC")
    List<Observation> findRecentObservations(@Param("isValid") boolean isValid);
    
    /**
     * Find top 5 most recent valid observations
     * Note: LIMIT clause in JPQL works differently across JPA implementations
     * For better compatibility, use PageRequest instead
     * @return list of most recent valid observations
     */
    @Query(value = "SELECT * FROM observations WHERE is_valid = true ORDER BY submission_timestamp DESC LIMIT 5", nativeQuery = true)
    List<Observation> findTop5RecentValidObservations();
}
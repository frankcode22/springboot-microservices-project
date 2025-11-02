package com.citizensciencewater.rewards.repository;


import com.citizensciencewater.rewards.models.CitizenReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CitizenReward entity.
 * Provides CRUD operations and custom queries for citizen rewards data.
 * 
 * @author KF7014 Assessment
 * @version 2.0 - JPA Implementation
 */
@Repository
public interface CitizenRewardRepository extends JpaRepository<CitizenReward, Long> {

    /**
     * Find a citizen reward record by citizen ID
     * @param citizenId the unique citizen identifier
     * @return Optional containing the CitizenReward if found
     */
    Optional<CitizenReward> findByCitizenId(String citizenId);

    /**
     * Check if a citizen reward record exists for a given citizen ID
     * @param citizenId the unique citizen identifier
     * @return true if record exists, false otherwise
     */
    boolean existsByCitizenId(String citizenId);

    /**
     * Find all citizens with a specific badge level
     * @param currentBadge the badge level (e.g., "Bronze", "Silver", "Gold")
     * @return List of CitizenReward records with that badge level
     */
    List<CitizenReward> findByCurrentBadge(String currentBadge);

    /**
     * Find all citizens with points greater than or equal to specified amount
     * @param points minimum points threshold
     * @return List of CitizenReward records meeting the criteria
     */
    List<CitizenReward> findByTotalPointsGreaterThanEqual(int points);

    /**
     * Find top N citizens by total points (leaderboard)
     * @param limit number of top citizens to retrieve
     * @return List of top citizen rewards ordered by points descending
     */
    @Query("SELECT cr FROM CitizenReward cr ORDER BY cr.totalPoints DESC LIMIT :limit")
    List<CitizenReward> findTopCitizensByPoints(@Param("limit") int limit);

    /**
     * Find all citizens ordered by total points descending
     * @return List of all CitizenReward records ordered by points
     */
    List<CitizenReward> findAllByOrderByTotalPointsDesc();

    /**
     * Count citizens by badge level
     * @param currentBadge the badge level
     * @return count of citizens with that badge
     */
    long countByCurrentBadge(String currentBadge);

    /**
     * Find citizens with at least N valid observations
     * @param minObservations minimum number of valid observations
     * @return List of CitizenReward records meeting the criteria
     */
    List<CitizenReward> findByValidObservationsGreaterThanEqual(int minObservations);

    /**
     * Get total points across all citizens
     * @return sum of all points
     */
    @Query("SELECT SUM(cr.totalPoints) FROM CitizenReward cr")
    Long getTotalPointsAcrossAllCitizens();

    /**
     * Get total valid observations across all citizens
     * @return sum of all valid observations
     */
    @Query("SELECT SUM(cr.validObservations) FROM CitizenReward cr")
    Long getTotalValidObservationsAcrossAllCitizens();
}
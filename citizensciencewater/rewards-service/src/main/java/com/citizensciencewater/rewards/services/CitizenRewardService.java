package com.citizensciencewater.rewards.services;


import com.citizensciencewater.rewards.models.CitizenReward;
import com.citizensciencewater.rewards.repository.CitizenRewardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing citizen rewards.
 * Handles business logic for reward calculations, badge awarding, and leaderboards.
 * * @author KF7014 Assessment
 * @version 2.0 - JPA Implementation
 */
@Service
@Transactional
public class CitizenRewardService {

    private final CitizenRewardRepository rewardRepository;

    @Autowired
    public CitizenRewardService(CitizenRewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    // =====================
    // Core CRUD Operations
    // =====================

    /**
     * Get reward information for a specific citizen.
     * Creates a new record if citizen doesn't exist.
     * * @param citizenId the unique citizen identifier
     * @return CitizenReward object
     */
    public CitizenReward getCitizenReward(String citizenId) {
        return rewardRepository.findByCitizenId(citizenId)
                .orElseGet(() -> createNewCitizenReward(citizenId));
    }

    /**
     * Get reward information for a citizen without creating new record.
     * * @param citizenId the unique citizen identifier
     * @return Optional containing CitizenReward if found
     */
    @Transactional(readOnly = true)
    public Optional<CitizenReward> findCitizenReward(String citizenId) {
        return rewardRepository.findByCitizenId(citizenId);
    }

    /**
     * Create a new citizen reward record.
     * * @param citizenId the unique citizen identifier
     * @return newly created CitizenReward object
     */
    public CitizenReward createNewCitizenReward(String citizenId) {
        CitizenReward reward = new CitizenReward(citizenId);
        return rewardRepository.save(reward);
    }

    /**
     * Get all citizen rewards.
     * * @return List of all CitizenReward records
     */
    @Transactional(readOnly = true)
    public List<CitizenReward> getAllCitizenRewards() {
        return rewardRepository.findAll();
    }

    /**
     * Delete a citizen reward record.
     * * @param citizenId the unique citizen identifier
     * @return true if deleted, false if not found
     */
    public boolean deleteCitizenReward(String citizenId) {
        Optional<CitizenReward> reward = rewardRepository.findByCitizenId(citizenId);
        if (reward.isPresent()) {
            rewardRepository.delete(reward.get());
            return true;
        }
        return false;
    }

    // =====================
    // Observation Processing
    // =====================

    /**
     * Add a valid observation for a citizen and award points.
     * * @param citizenId the unique citizen identifier
     * @param isComplete whether the observation is complete
     * @return updated CitizenReward object
     */
    public CitizenReward addObservation(String citizenId, boolean isComplete) {
        CitizenReward reward = getCitizenReward(citizenId);
        reward.addObservation(isComplete);
        return rewardRepository.save(reward);
    }

    /**
     * Add multiple observations for a citizen.
     * * @param citizenId the unique citizen identifier
     * @param validCount number of valid observations
     * @param completeCount number of complete observations
     * @return updated CitizenReward object
     */
    public CitizenReward addMultipleObservations(String citizenId, int validCount, int completeCount) {
        CitizenReward reward = getCitizenReward(citizenId);
        
        for (int i = 0; i < validCount; i++) {
            boolean isComplete = i < completeCount;
            reward.addObservation(isComplete);
        }
        
        return rewardRepository.save(reward);
    }

    // =====================
    // Leaderboard & Rankings
    // =====================

    /**
     * Get the top N citizens by total points.
     * * @param limit number of top citizens to retrieve
     * @return List of top CitizenReward records
     */
    @Transactional(readOnly = true)
    public List<CitizenReward> getLeaderboard(int limit) {
        return rewardRepository.findTopCitizensByPoints(limit);
    }

    /**
     * Get all citizens ordered by points (full leaderboard).
     * * @return List of all CitizenReward records ordered by points descending
     */
    @Transactional(readOnly = true)
    public List<CitizenReward> getFullLeaderboard() {
        return rewardRepository.findAllByOrderByTotalPointsDesc();
    }

    /**
     * Get rank of a specific citizen.
     * * @param citizenId the unique citizen identifier
     * @return rank (1-based), or -1 if not found
     */
    @Transactional(readOnly = true)
    public int getCitizenRank(String citizenId) {
        List<CitizenReward> leaderboard = getFullLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getCitizenId().equals(citizenId)) {
                return i + 1;
            }
        }
        return -1;
    }

    // =====================
    // Badge & Statistics Queries
    // =====================

    /**
     * Get all citizens with a specific badge level.
     * * @param badgeLevel the badge level (e.g., "Bronze", "Silver", "Gold")
     * @return List of CitizenReward records with that badge
     */
    @Transactional(readOnly = true)
    public List<CitizenReward> getCitizensByBadge(String badgeLevel) {
        return rewardRepository.findByCurrentBadge(badgeLevel);
    }

    /**
     * Get count of citizens by badge level.
     * * @param badgeLevel the badge level
     * @return count of citizens with that badge
     */
    @Transactional(readOnly = true)
    public long countCitizensByBadge(String badgeLevel) {
        return rewardRepository.countByCurrentBadge(badgeLevel);
    }

    /**
     * Get citizens with at least N points.
     * * @param minPoints minimum points threshold
     * @return List of CitizenReward records meeting criteria
     */
    @Transactional(readOnly = true)
    public List<CitizenReward> getCitizensWithMinimumPoints(int minPoints) {
        return rewardRepository.findByTotalPointsGreaterThanEqual(minPoints);
    }

    /**
     * Get citizens with at least N valid observations.
     * * @param minObservations minimum number of observations
     * @return List of CitizenReward records meeting criteria
     */
    @Transactional(readOnly = true)
    public List<CitizenReward> getCitizensWithMinimumObservations(int minObservations) {
        return rewardRepository.findByValidObservationsGreaterThanEqual(minObservations);
    }

    // =====================
    // Global Statistics
    // =====================

    /**
     * Get total points across all citizens.
     * * @return sum of all points
     */
    @Transactional(readOnly = true)
    public long getTotalPointsAcrossAllCitizens() {
        Long total = rewardRepository.getTotalPointsAcrossAllCitizens();
        return total != null ? total : 0L;
    }

    /**
     * Get total valid observations across all citizens.
     * * @return sum of all valid observations
     */
    @Transactional(readOnly = true)
    public long getTotalValidObservationsAcrossAllCitizens() {
        Long total = rewardRepository.getTotalValidObservationsAcrossAllCitizens();
        return total != null ? total : 0L;
    }

    /**
     * Get total number of citizens in the system.
     * * @return count of citizen reward records
     */
    @Transactional(readOnly = true)
    public long getTotalCitizensCount() {
        return rewardRepository.count();
    }

    /**
     * Get badge distribution statistics.
     * * @return array with counts [none, bronze, silver, gold]
     */
    @Transactional(readOnly = true)
    public long[] getBadgeDistribution() {
        return new long[] {
            countCitizensByBadge("None"),
            countCitizensByBadge("Bronze"),
            countCitizensByBadge("Silver"),
            countCitizensByBadge("Gold")
        };
    }

    // =====================
    // Manual Point/Badge Management
    // =====================

    /**
     * Manually add points to a citizen (for admin adjustments).
     * * @param citizenId the unique citizen identifier
     * @param points points to add
     * @return updated CitizenReward object
     */
    public CitizenReward addPoints(String citizenId, int points) {
        CitizenReward reward = getCitizenReward(citizenId);
        reward.setTotalPoints(reward.getTotalPoints() + points);
        // Note: Assumes the CitizenReward model handles badge recalculation in its setters or a specific update method.
        return rewardRepository.save(reward);
    }
    
    /**
     * Processes various types of rewards (points, observations, etc.) based on the input map.
     * This method acts as a unified endpoint for external systems to grant rewards.
     *
     * @param citizenId the unique citizen identifier
     * @param rewardData Map containing reward keys (e.g., "points", "validObservations", "completeObservations") and values
     * @return updated CitizenReward object
     */
    public CitizenReward processReward(String citizenId, Map<String, Object> rewardData) {
        CitizenReward reward = getCitizenReward(citizenId);

        // Process Points (Expecting Integer)
        Object pointsValue = rewardData.get("points");
        if (pointsValue instanceof Integer) {
            int points = (Integer) pointsValue;
            if (points != 0) {
                reward.setTotalPoints(reward.getTotalPoints() + points);
            }
        }

        // Process Valid Observations (Expecting Integer)
        Object validObsValue = rewardData.get("validObservations");
        if (validObsValue instanceof Integer) {
            int validObs = (Integer) validObsValue;
            if (validObs > 0) {
                reward.setValidObservations(reward.getValidObservations() + validObs);
            }
        }

        // Process Complete Observations (Expecting Integer)
        Object completeObsValue = rewardData.get("completeObservations");
        if (completeObsValue instanceof Integer) {
            int completeObs = (Integer) completeObsValue;
            if (completeObs > 0) {
                reward.setCompleteObservations(reward.getCompleteObservations() + completeObs);
            }
        }

        // IMPORTANT: If changes were made, ensure the CitizenReward entity or a separate utility method 
        // recalculates the current badge status before saving.

        return rewardRepository.save(reward);
    }
    

    /**
     * Reset a citizen's rewards (for testing or admin purposes).
     * * @param citizenId the unique citizen identifier
     * @return reset CitizenReward object
     */
    public CitizenReward resetCitizenReward(String citizenId) {
        Optional<CitizenReward> existingReward = rewardRepository.findByCitizenId(citizenId);
        
        CitizenReward reward;
        if (existingReward.isPresent()) {
            reward = existingReward.get();
            reward.setTotalPoints(0);
            reward.setValidObservations(0);
            reward.setCompleteObservations(0);
            reward.setBadges("");
            reward.setCurrentBadge("None");
        } else {
            reward = new CitizenReward(citizenId);
        }
        
        return rewardRepository.save(reward);
    }
}

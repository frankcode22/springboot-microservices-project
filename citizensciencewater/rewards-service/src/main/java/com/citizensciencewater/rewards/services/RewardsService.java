package com.citizensciencewater.rewards.services;

import com.citizensciencewater.rewards.models.CitizenReward;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class for managing citizen rewards.
 * Reads observation data from the Crowdsourced Data microservice
 * and calculates points and badges for citizens.
 * Maintains reward data in-memory (not persisted).
 */
@Service
public class RewardsService {

    private static final Logger log = LoggerFactory.getLogger(RewardsService.class);

//    @Value("${crowdsourced.data.service.url}")
    @Value("${crowdsourced.data.service.url}")
    private String crowdsourcedDataServiceUrl;

    private final RestTemplate restTemplate;

    // In-memory storage for citizen rewards
    private final Map<String, CitizenReward> citizenRewards = new ConcurrentHashMap<>();

    /**
     * Constructor initializing RestTemplate for HTTP requests
     */
    public RewardsService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Calculate and update rewards for a specific citizen.
     * Fetches observations from Crowdsourced Data Service.
     *
     * @param citizenId the unique identifier of the citizen
     * @return updated CitizenReward object
     */
    public CitizenReward calculateRewardsForCitizen(String citizenId) {
        log.info("Calculating rewards for citizen: {}", citizenId);

        try {
            // Fetch observations from Crowdsourced Data Service
            String url = crowdsourcedDataServiceUrl + "/citizen/" + citizenId + "/valid";
            log.debug("Fetching observations from: {}", url);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> observations = response.getBody();

            if (observations == null || observations.isEmpty()) {
                log.info("No valid observations found for citizen: {}", citizenId);
                return citizenRewards.getOrDefault(citizenId, new CitizenReward(citizenId));
            }

            // Create or get existing reward record
            CitizenReward reward = citizenRewards.getOrDefault(citizenId, new CitizenReward(citizenId));

            // Reset counts before recalculation
            reward.setTotalPoints(0);
            reward.setValidObservations(0);
            reward.setCompleteObservations(0);
            reward.setBadges(new ArrayList<>());
            reward.setCurrentBadge("None");

            // Calculate points based on observations
            for (Map<String, Object> observation : observations) {
                boolean isComplete = (Boolean) observation.getOrDefault("complete", false);
                reward.addObservation(isComplete);
            }

            // Update in-memory storage
            citizenRewards.put(citizenId, reward);

            log.info("Rewards calculated - Citizen: {}, Points: {}, Badge: {}",
                    citizenId, reward.getTotalPoints(), reward.getCurrentBadge());

            return reward;

        } catch (Exception e) {
            log.error("Error calculating rewards for citizen {}: {}", citizenId, e.getMessage(), e);
            return citizenRewards.getOrDefault(citizenId, new CitizenReward(citizenId));
        }
    }

    /**
     * Get rewards for a specific citizen.
     * If not in cache, calculate rewards.
     *
     * @param citizenId the unique identifier of the citizen
     * @return CitizenReward object
     */
    public CitizenReward getRewardsForCitizen(String citizenId) {
        log.info("Getting rewards for citizen: {}", citizenId);

        if (citizenRewards.containsKey(citizenId)) {
            log.debug("Returning cached rewards for citizen: {}", citizenId);
            return citizenRewards.get(citizenId);
        }

        // Calculate if not in cache
        log.debug("Citizen {} not in cache, calculating rewards", citizenId);
        return calculateRewardsForCitizen(citizenId);
    }

    /**
     * Get leaderboard of top N contributors.
     * Sorted by total points in descending order.
     *
     * @param topN number of top contributors to return
     * @return list of top N CitizenReward objects
     */
    public List<CitizenReward> getLeaderboard(int topN) {
        log.info("Generating leaderboard for top {} contributors", topN);

        // Refresh all citizen rewards before generating leaderboard
        refreshAllRewards();

        List<CitizenReward> leaderboard = citizenRewards.values().stream()
                .sorted((r1, r2) -> Integer.compare(r2.getTotalPoints(), r1.getTotalPoints()))
                .limit(topN)
                .toList();

        log.info("Leaderboard generated with {} entries", leaderboard.size());
        return leaderboard;
    }

    /**
     * Get top 3 contributors for leaderboard.
     *
     * @return list of top 3 CitizenReward objects
     */
    public List<CitizenReward> getTopThreeLeaderboard() {
        log.debug("Fetching top 3 leaderboard");
        return getLeaderboard(3);
    }

    /**
     * Refresh rewards for all citizens in the system.
     * Fetches all observations and updates rewards.
     */
    public void refreshAllRewards() {
        log.info("Refreshing rewards for all citizens");

        try {
            // Fetch all valid observations
            String url = crowdsourcedDataServiceUrl + "/valid";
            log.debug("Fetching all valid observations from: {}", url);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> observations = response.getBody();

            if (observations == null || observations.isEmpty()) {
                log.info("No observations found to refresh");
                return;
            }

            log.debug("Processing {} observations", observations.size());

            // Group observations by citizen ID
            Map<String, List<Map<String, Object>>> observationsByCitizen = new HashMap<>();
            for (Map<String, Object> obs : observations) {
                String citizenId = (String) obs.get("citizenId");
                observationsByCitizen.computeIfAbsent(citizenId, k -> new ArrayList<>()).add(obs);
            }

            // Calculate rewards for each citizen
            for (Map.Entry<String, List<Map<String, Object>>> entry : observationsByCitizen.entrySet()) {
                String citizenId = entry.getKey();
                CitizenReward reward = new CitizenReward(citizenId);

                for (Map<String, Object> observation : entry.getValue()) {
                    boolean isComplete = (Boolean) observation.getOrDefault("complete", false);
                    reward.addObservation(isComplete);
                }

                citizenRewards.put(citizenId, reward);
                log.debug("Updated rewards for citizen {}: {} points, {} badge",
                        citizenId, reward.getTotalPoints(), reward.getCurrentBadge());
            }

            log.info("Rewards refreshed for {} citizens", observationsByCitizen.size());

        } catch (Exception e) {
            log.error("Error refreshing all rewards: {}", e.getMessage(), e);
        }
    }

    /**
     * Clear all rewards data from memory.
     */
    public void clearAllRewards() {
        int sizeBefore = citizenRewards.size();
        log.info("Clearing all rewards data for {} citizens", sizeBefore);
        citizenRewards.clear();
        log.info("All rewards data cleared successfully");
    }

    /**
     * Get statistics for all citizens.
     * @return map with total citizens, total points, and average points.
     */
    public Map<String, Object> getStatistics() {
        log.debug("Calculating statistics for {} citizens", citizenRewards.size());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCitizens", citizenRewards.size());

        int totalPoints = citizenRewards.values().stream()
                .mapToInt(CitizenReward::getTotalPoints)
                .sum();
        stats.put("totalPoints", totalPoints);

        double averagePoints = citizenRewards.isEmpty() ? 0 :
                (double) totalPoints / citizenRewards.size();
        stats.put("averagePoints", Math.round(averagePoints * 100.0) / 100.0);

        log.info("Statistics calculated - Total Citizens: {}, Total Points: {}, Avg Points: {}",
                citizenRewards.size(), totalPoints, averagePoints);

        return stats;
    }
}

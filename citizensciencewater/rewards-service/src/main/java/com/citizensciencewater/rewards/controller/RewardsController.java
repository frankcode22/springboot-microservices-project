package com.citizensciencewater.rewards.controller;

import com.citizensciencewater.rewards.models.CitizenReward;
import com.citizensciencewater.rewards.services.RewardsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for handling citizen rewards requests.
 * Provides endpoints for calculating and retrieving reward information.
 */
@RestController
@RequestMapping("/api/rewards")
@CrossOrigin(origins = "*")
public class RewardsController {
    
    private static final Logger log = LoggerFactory.getLogger(RewardsController.class);
    
    private final RewardsService rewardsService;
    
    /**
     * Constructor with dependency injection
     * @param rewardsService the service handling rewards business logic
     */
    @Autowired
    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }
    
    /**
     * GET endpoint to retrieve rewards for a specific citizen
     * Calculates points and badges based on their observations
     * 
     * @param citizenId the unique identifier of the citizen
     * @return CitizenReward object with points and badges
     * 
     * Example Response:
     * {
     *   "citizenId": "citizen123",
     *   "totalPoints": 50,
     *   "validObservations": 3,
     *   "completeObservations": 2,
     *   "badges": ["Bronze"],
     *   "currentBadge": "Bronze",
     *   "pointsToNextBadge": 50,
     *   "nextBadge": "Silver"
     * }
     */
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<CitizenReward> getRewardsForCitizen(@PathVariable String citizenId) {
        log.info("GET /api/rewards/citizen/{} - Fetching rewards", citizenId);
        CitizenReward reward = rewardsService.getRewardsForCitizen(citizenId);
        return ResponseEntity.ok(reward);
    }
    
    /**
     * POST endpoint to calculate/recalculate rewards for a specific citizen
     * Forces a refresh of data from the Crowdsourced Data Service
     * 
     * @param citizenId the unique identifier of the citizen
     * @return CitizenReward object with updated points and badges
     */
    @PostMapping("/citizen/{citizenId}/calculate")
    public ResponseEntity<CitizenReward> calculateRewardsForCitizen(@PathVariable String citizenId) {
        log.info("POST /api/rewards/citizen/{}/calculate - Calculating rewards", citizenId);
        CitizenReward reward = rewardsService.calculateRewardsForCitizen(citizenId);
        return ResponseEntity.ok(reward);
    }
    
    /**
     * GET endpoint to retrieve the leaderboard of top contributors
     * 
     * @param topN number of top contributors to return (default: 10)
     * @return list of top N CitizenReward objects sorted by points
     * 
     * Example Response:
     * [
     *   {
     *     "citizenId": "citizen456",
     *     "totalPoints": 520,
     *     "currentBadge": "Gold",
     *     ...
     *   },
     *   {
     *     "citizenId": "citizen123",
     *     "totalPoints": 240,
     *     "currentBadge": "Silver",
     *     ...
     *   }
     * ]
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<CitizenReward>> getLeaderboard(
            @RequestParam(defaultValue = "10") int topN) {
        log.info("GET /api/rewards/leaderboard - Fetching top {} contributors", topN);
        List<CitizenReward> leaderboard = rewardsService.getLeaderboard(topN);
        return ResponseEntity.ok(leaderboard);
    }
    
    /**
     * GET endpoint to retrieve top 3 contributors
     * Used specifically for dashboard display
     * 
     * @return list of top 3 CitizenReward objects
     */
    @GetMapping("/leaderboard/top3")
    public ResponseEntity<List<CitizenReward>> getTopThreeLeaderboard() {
        log.info("GET /api/rewards/leaderboard/top3 - Fetching top 3 contributors");
        List<CitizenReward> leaderboard = rewardsService.getTopThreeLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
    
    /**
     * POST endpoint to refresh rewards for all citizens
     * Fetches all observations and recalculates all rewards
     * 
     * @return success message
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAllRewards() {
        log.info("POST /api/rewards/refresh - Refreshing all rewards");
        rewardsService.refreshAllRewards();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All rewards refreshed successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE endpoint to clear all rewards data
     * Useful for testing or reset purposes
     * 
     * @return success message
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAllRewards() {
        log.info("DELETE /api/rewards/clear - Clearing all rewards");
        rewardsService.clearAllRewards();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All rewards cleared successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET endpoint to retrieve statistics about rewards
     * 
     * @return map with total citizens, total points, and average points
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        log.info("GET /api/rewards/statistics - Fetching statistics");
        Map<String, Object> stats = rewardsService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}
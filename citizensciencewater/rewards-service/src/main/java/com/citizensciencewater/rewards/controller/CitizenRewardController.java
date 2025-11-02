package com.citizensciencewater.rewards.controller;


import com.citizensciencewater.rewards.models.CitizenReward;
import com.citizensciencewater.rewards.services.CitizenRewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for citizen rewards management.
 * Provides endpoints for reward tracking, leaderboards, and statistics.
 * * @author KF7014 Assessment
 * @version 2.0 - JPA Implementation
 */
@RestController
@RequestMapping("/api/rewards")
@CrossOrigin(origins = "*")
public class CitizenRewardController {

    private final CitizenRewardService rewardService;

    @Autowired
    public CitizenRewardController(CitizenRewardService rewardService) {
        this.rewardService = rewardService;
    }

    // =====================
    // Citizen Reward CRUD Endpoints
    // =====================

    /**
     * Get reward information for a specific citizen.
     * Creates new record if citizen doesn't exist.
     * * GET /api/rewards/citizen/{citizenId}
     * * @param citizenId the unique citizen identifier
     * @return CitizenReward object
     */
    @GetMapping("/citizen/{citizenId}")
    public ResponseEntity<CitizenReward> getCitizenReward(@PathVariable String citizenId) {
        try {
            CitizenReward reward = rewardService.getCitizenReward(citizenId);
            return ResponseEntity.ok(reward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all citizen rewards.
     * * GET /api/rewards/all
     * * @return List of all CitizenReward records
     */
    @GetMapping("/all")
    public ResponseEntity<List<CitizenReward>> getAllCitizenRewards() {
        try {
            List<CitizenReward> rewards = rewardService.getAllCitizenRewards();
            return ResponseEntity.ok(rewards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new citizen reward record.
     * * POST /api/rewards/citizen
     * Body: { "citizenId": "citizen123" }
     * * @param request Map containing citizenId
     * @return newly created CitizenReward object
     */
    @PostMapping("/citizen")
    public ResponseEntity<CitizenReward> createCitizenReward(@RequestBody Map<String, String> request) {
        try {
            String citizenId = request.get("citizenId");
            if (citizenId == null || citizenId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            CitizenReward reward = rewardService.createNewCitizenReward(citizenId);
            return ResponseEntity.status(HttpStatus.CREATED).body(reward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a citizen reward record.
     * * DELETE /api/rewards/citizen/{citizenId}
     * * @param citizenId the unique citizen identifier
     * @return success message or error
     */
    @DeleteMapping("/citizen/{citizenId}")
    public ResponseEntity<Map<String, String>> deleteCitizenReward(@PathVariable String citizenId) {
        try {
            boolean deleted = rewardService.deleteCitizenReward(citizenId);
            Map<String, String> response = new HashMap<>();
            
            if (deleted) {
                response.put("message", "Citizen reward deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Citizen reward not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // =====================
    // Observation Processing Endpoints
    // =====================

    /**
     * Add an observation for a citizen and award points.
     * * POST /api/rewards/citizen/{citizenId}/observation
     * Body: { "isComplete": true }
     * * @param citizenId the unique citizen identifier
     * @param request Map containing isComplete flag
     * @return updated CitizenReward object
     */
    @PostMapping("/citizen/{citizenId}/observation")
    public ResponseEntity<CitizenReward> addObservation(
            @PathVariable String citizenId,
            @RequestBody Map<String, Boolean> request) {
        try {
            boolean isComplete = request.getOrDefault("isComplete", false);
            CitizenReward reward = rewardService.addObservation(citizenId, isComplete);
            return ResponseEntity.ok(reward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add multiple observations for a citizen.
     * * POST /api/rewards/citizen/{citizenId}/observations/batch
     * Body: { "validCount": 5, "completeCount": 3 }
     * * @param citizenId the unique citizen identifier
     * @param request Map containing validCount and completeCount
     * @return updated CitizenReward object
     */
    @PostMapping("/citizen/{citizenId}/observations/batch")
    public ResponseEntity<CitizenReward> addMultipleObservations(
            @PathVariable String citizenId,
            @RequestBody Map<String, Integer> request) {
        try {
            int validCount = request.getOrDefault("validCount", 0);
            int completeCount = request.getOrDefault("completeCount", 0);
            
            if (validCount < 0 || completeCount < 0 || completeCount > validCount) {
                return ResponseEntity.badRequest().build();
            }
            
            CitizenReward reward = rewardService.addMultipleObservations(citizenId, validCount, completeCount);
            return ResponseEntity.ok(reward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =====================
    // Leaderboard Endpoints
    // =====================

    /**
     * Get the top N citizens by total points.
     * * GET /api/rewards/leaderboard?limit=10
     * * @param limit number of top citizens to retrieve (default: 10)
     * @return List of top CitizenReward records
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<CitizenReward>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            if (limit <= 0) {
                return ResponseEntity.badRequest().build();
            }
            List<CitizenReward> leaderboard = rewardService.getLeaderboard(limit);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get full leaderboard (all citizens ordered by points).
     * * GET /api/rewards/leaderboard/full
     * * @return List of all CitizenReward records ordered by points
     */
    @GetMapping("/leaderboard/full")
    public ResponseEntity<List<CitizenReward>> getFullLeaderboard() {
        try {
            List<CitizenReward> leaderboard = rewardService.getFullLeaderboard();
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get rank of a specific citizen.
     * * GET /api/rewards/citizen/{citizenId}/rank
     * * @param citizenId the unique citizen identifier
     * @return Map containing rank and total citizens
     */
    @GetMapping("/citizen/{citizenId}/rank")
    public ResponseEntity<Map<String, Object>> getCitizenRank(@PathVariable String citizenId) {
        try {
            int rank = rewardService.getCitizenRank(citizenId);
            long totalCitizens = rewardService.getTotalCitizensCount();
            
            Map<String, Object> response = new HashMap<>();
            if (rank == -1) {
                response.put("error", "Citizen not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("citizenId", citizenId);
            response.put("rank", rank);
            response.put("totalCitizens", totalCitizens);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =====================
    // Badge & Filter Endpoints
    // =====================

    /**
     * Get all citizens with a specific badge level.
     * * GET /api/rewards/badge/{badgeLevel}
     * * @param badgeLevel the badge level (None, Bronze, Silver, Gold)
     * @return List of CitizenReward records with that badge
     */
    @GetMapping("/badge/{badgeLevel}")
    public ResponseEntity<List<CitizenReward>> getCitizensByBadge(@PathVariable String badgeLevel) {
        try {
            List<CitizenReward> citizens = rewardService.getCitizensByBadge(badgeLevel);
            return ResponseEntity.ok(citizens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get citizens with at least N points.
     * * GET /api/rewards/filter/points?min=100
     * * @param minPoints minimum points threshold
     * @return List of CitizenReward records meeting criteria
     */
    @GetMapping("/filter/points")
    public ResponseEntity<List<CitizenReward>> getCitizensByMinimumPoints(
            @RequestParam int minPoints) {
        try {
            if (minPoints < 0) {
                return ResponseEntity.badRequest().build();
            }
            List<CitizenReward> citizens = rewardService.getCitizensWithMinimumPoints(minPoints);
            return ResponseEntity.ok(citizens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get citizens with at least N observations.
     * * GET /api/rewards/filter/observations?min=5
     * * @param minObservations minimum number of observations
     * @return List of CitizenReward records meeting criteria
     */
    @GetMapping("/filter/observations")
    public ResponseEntity<List<CitizenReward>> getCitizensByMinimumObservations(
            @RequestParam int minObservations) {
        try {
            if (minObservations < 0) {
                return ResponseEntity.badRequest().build();
            }
            List<CitizenReward> citizens = rewardService.getCitizensWithMinimumObservations(minObservations);
            return ResponseEntity.ok(citizens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =====================
    // Statistics Endpoints
    // =====================

    /**
     * Get overall system statistics.
     * * GET /api/rewards/statistics
     * * @return Map containing various statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalCitizens", rewardService.getTotalCitizensCount());
            stats.put("totalPoints", rewardService.getTotalPointsAcrossAllCitizens());
            stats.put("totalObservations", rewardService.getTotalValidObservationsAcrossAllCitizens());
            
            long[] badgeDistribution = rewardService.getBadgeDistribution();
            Map<String, Long> badges = new HashMap<>();
            badges.put("none", badgeDistribution[0]);
            badges.put("bronze", badgeDistribution[1]);
            badges.put("silver", badgeDistribution[2]);
            badges.put("gold", badgeDistribution[3]);
            stats.put("badgeDistribution", badges);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get badge distribution only.
     * * GET /api/rewards/statistics/badges
     * * @return Map containing badge counts
     */
    @GetMapping("/statistics/badges")
    public ResponseEntity<Map<String, Long>> getBadgeDistribution() {
        try {
            long[] badgeDistribution = rewardService.getBadgeDistribution();
            Map<String, Long> badges = new HashMap<>();
            badges.put("none", badgeDistribution[0]);
            badges.put("bronze", badgeDistribution[1]);
            badges.put("silver", badgeDistribution[2]);
            badges.put("gold", badgeDistribution[3]);
            
            return ResponseEntity.ok(badges);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =====================
    // Admin Endpoints
    // =====================

    /**
     * Manually process a reward transaction for a citizen (points and/or badge).
     * This endpoint is used by the CitizenRewarder frontend component.
     * * POST /api/rewards/citizen/{citizenId}/reward
     * Body: { "points": 50, "badge": "Gold" }
     * * @param citizenId the unique citizen identifier
     * @param request Map containing 'points' and optional 'badge'
     * @return updated CitizenReward object
     */
    @PostMapping("/citizen/{citizenId}/reward")
    public ResponseEntity<CitizenReward> rewardCitizen(
            @PathVariable String citizenId,
            @RequestBody Map<String, Object> request) {
        
        try {
            // Check for required 'points' field and ensure it's a positive number (assuming Object is Integer)
            if (!request.containsKey("points") || !(request.get("points") instanceof Integer) || (Integer)request.get("points") <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            // This assumes your CitizenRewardService has a method named processReward
            CitizenReward updatedReward = rewardService.processReward(citizenId, request); 
            return ResponseEntity.ok(updatedReward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Manually add points to a citizen (admin only).
     * * POST /api/rewards/citizen/{citizenId}/points
     * Body: { "points": 50 }
     * * @param citizenId the unique citizen identifier
     * @param request Map containing points to add
     * @return updated CitizenReward object
     */
    @PostMapping("/citizen/{citizenId}/points")
    public ResponseEntity<CitizenReward> addPoints(
            @PathVariable String citizenId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer points = request.get("points");
            if (points == null) {
                return ResponseEntity.badRequest().build();
            }
            CitizenReward reward = rewardService.addPoints(citizenId, points);
            return ResponseEntity.ok(reward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reset a citizen's rewards (admin only).
     * * POST /api/rewards/citizen/{citizenId}/reset
     * * @param citizenId the unique citizen identifier
     * @return reset CitizenReward object
     */
    @PostMapping("/citizen/{citizenId}/reset")
    public ResponseEntity<CitizenReward> resetCitizenReward(@PathVariable String citizenId) {
        try {
            CitizenReward reward = rewardService.resetCitizenReward(citizenId);
            return ResponseEntity.ok(reward);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =====================
    // Health Check
    // =====================

    /**
     * Health check endpoint.
     * * GET /api/rewards/health
     * * @return status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "Citizen Rewards API");
        return ResponseEntity.ok(response);
    }
}

package com.citizensciencewater.rewards.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a citizen's reward status.
 * Tracks points earned and achievement badges for a citizen's contributions.
 * Data is maintained in-memory (not persisted to database).
 */
public class CitizenReward {

    /**
     * Unique identifier for the citizen
     */
    private String citizenId;

    /**
     * Total points earned by the citizen
     */
    private int totalPoints;

    /**
     * Number of valid observations submitted
     */
    private int validObservations;

    /**
     * Number of complete observations submitted
     */
    private int completeObservations;

    /**
     * List of achievement badges earned
     * e.g., Bronze, Silver, Gold
     */
    private List<String> badges;

    /**
     * Current badge level of the citizen
     */
    private String currentBadge;

    /**
     * Default constructor
     */
    public CitizenReward() {
        this.badges = new ArrayList<>();
        this.currentBadge = "None";
    }

    /**
     * Constructor with all fields
     */
    public CitizenReward(String citizenId, int totalPoints, int validObservations,
                         int completeObservations, List<String> badges, String currentBadge) {
        this.citizenId = citizenId;
        this.totalPoints = totalPoints;
        this.validObservations = validObservations;
        this.completeObservations = completeObservations;
        this.badges = badges != null ? badges : new ArrayList<>();
        this.currentBadge = currentBadge != null ? currentBadge : "None";
    }

    /**
     * Constructor with citizen ID
     * Initializes points and badges
     */
    public CitizenReward(String citizenId) {
        this.citizenId = citizenId;
        this.totalPoints = 0;
        this.validObservations = 0;
        this.completeObservations = 0;
        this.badges = new ArrayList<>();
        this.currentBadge = "None";
    }

    /**
     * Add points for a valid observation
     * Awards 10 points for valid observation
     * Awards additional 10 bonus points if complete
     */
    public void addObservation(boolean isComplete) {
        this.validObservations++;
        this.totalPoints += 10; // Base points for valid observation

        if (isComplete) {
            this.completeObservations++;
            this.totalPoints += 10; // Bonus points for complete observation
        }

        updateBadges();
    }

    /**
     * Update badge level based on total points
     * Bronze: 100 points
     * Silver: 200 points
     * Gold: 500 points
     */
    private void updateBadges() {
        if (totalPoints >= 500 && !badges.contains("Gold")) {
            badges.add("Gold");
            currentBadge = "Gold";
        } else if (totalPoints >= 200 && !badges.contains("Silver")) {
            badges.add("Silver");
            currentBadge = "Silver";
        } else if (totalPoints >= 100 && !badges.contains("Bronze")) {
            badges.add("Bronze");
            currentBadge = "Bronze";
        }
    }

    /**
     * Calculate points needed for next badge
     * @return points needed for next badge, or 0 if at maximum level
     */
    public int getPointsToNextBadge() {
        if (totalPoints < 100) {
            return 100 - totalPoints;
        } else if (totalPoints < 200) {
            return 200 - totalPoints;
        } else if (totalPoints < 500) {
            return 500 - totalPoints;
        }
        return 0; // Already at Gold level
    }

    /**
     * Get the name of the next badge to achieve
     * @return name of next badge, or "Maximum Level" if at Gold
     */
    public String getNextBadge() {
        if (totalPoints < 100) {
            return "Bronze";
        } else if (totalPoints < 200) {
            return "Silver";
        } else if (totalPoints < 500) {
            return "Gold";
        }
        return "Maximum Level";
    }

    // -------------------------------
    // Getters and Setters
    // -------------------------------

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getValidObservations() {
        return validObservations;
    }

    public void setValidObservations(int validObservations) {
        this.validObservations = validObservations;
    }

    public int getCompleteObservations() {
        return completeObservations;
    }

    public void setCompleteObservations(int completeObservations) {
        this.completeObservations = completeObservations;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public String getCurrentBadge() {
        return currentBadge;
    }

    public void setCurrentBadge(String currentBadge) {
        this.currentBadge = currentBadge;
    }

    @Override
    public String toString() {
        return "CitizenReward{" +
                "citizenId='" + citizenId + '\'' +
                ", totalPoints=" + totalPoints +
                ", validObservations=" + validObservations +
                ", completeObservations=" + completeObservations +
                ", badges=" + badges +
                ", currentBadge='" + currentBadge + '\'' +
                '}';
    }
}

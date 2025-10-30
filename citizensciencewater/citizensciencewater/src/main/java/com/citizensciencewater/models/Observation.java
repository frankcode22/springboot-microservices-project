package com.citizensciencewater.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity class representing a water quality observation submitted by a citizen.
 * This class maps to the observations table in the database and stores all
 * information related to a single water quality submission.
 */
@Entity
@Table(name = "observations")
public class Observation {
    
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;
    
    @Column(name = "citizen_id", nullable = false)
    private String citizenId;
    
    @Column(name = "postcode", nullable = false)
    private String postcode;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "ph")
    private Double ph;
    
    @Column(name = "alkalinity")
    private Double alkalinity;
    
    @Column(name = "turbidity")
    private Double turbidity;
    
    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;
    
    @Column(name = "image_paths", columnDefinition = "TEXT")
    private String imagePaths;
    
    @Column(name = "submission_timestamp", nullable = false)
    private LocalDateTime submissionTimestamp;
    
    @Column(name = "is_valid", nullable = false)
    private boolean isValid;
    
    @Column(name = "is_complete", nullable = false)
    private boolean isComplete;
    
    // Constructors
    public Observation() {
    }
    
    public Observation(String id, String citizenId, String postcode, Double temperature, 
                      Double ph, Double alkalinity, Double turbidity, String observations, 
                      String imagePaths, LocalDateTime submissionTimestamp, 
                      boolean isValid, boolean isComplete) {
        this.id = id;
        this.citizenId = citizenId;
        this.postcode = postcode;
        this.temperature = temperature;
        this.ph = ph;
        this.alkalinity = alkalinity;
        this.turbidity = turbidity;
        this.observations = observations;
        this.imagePaths = imagePaths;
        this.submissionTimestamp = submissionTimestamp;
        this.isValid = isValid;
        this.isComplete = isComplete;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getCitizenId() {
        return citizenId;
    }
    
    public String getPostcode() {
        return postcode;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public Double getPh() {
        return ph;
    }
    
    public Double getAlkalinity() {
        return alkalinity;
    }
    
    public Double getTurbidity() {
        return turbidity;
    }
    
    public String getObservations() {
        return observations;
    }
    
    public String getImagePaths() {
        return imagePaths;
    }
    
    public LocalDateTime getSubmissionTimestamp() {
        return submissionTimestamp;
    }
    
    public boolean isValid() {
        return isValid;
    }
    
    public boolean isComplete() {
        return isComplete;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }
    
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public void setPh(Double ph) {
        this.ph = ph;
    }
    
    public void setAlkalinity(Double alkalinity) {
        this.alkalinity = alkalinity;
    }
    
    public void setTurbidity(Double turbidity) {
        this.turbidity = turbidity;
    }
    
    public void setObservations(String observations) {
        this.observations = observations;
    }
    
    public void setImagePaths(String imagePaths) {
        this.imagePaths = imagePaths;
    }
    
    public void setSubmissionTimestamp(LocalDateTime submissionTimestamp) {
        this.submissionTimestamp = submissionTimestamp;
    }
    
    public void setValid(boolean valid) {
        isValid = valid;
    }
    
    public void setComplete(boolean complete) {
        isComplete = complete;
    }
    
    /**
     * Pre-persist hook to generate UUID and set timestamp before saving
     */
    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.submissionTimestamp == null) {
            this.submissionTimestamp = LocalDateTime.now();
        }
    }
    
    /**
     * Checks if the observation has at least one measurement field
     * @return true if temperature, ph, alkalinity, or turbidity is present
     */
    public boolean hasMeasurements() {
        return temperature != null || ph != null || alkalinity != null || turbidity != null;
    }
    
    /**
     * Checks if the observation has visual observations
     * @return true if observations field is not empty
     */
    public boolean hasVisualObservations() {
        return observations != null && !observations.trim().isEmpty();
    }
    
    /**
     * Validates the observation based on requirement rules
     * Valid if: postcode is present AND (at least one measurement OR at least one observation)
     * @return true if valid, false otherwise
     */
    public boolean validate() {
        return postcode != null && !postcode.trim().isEmpty() 
               && (hasMeasurements() || hasVisualObservations());
    }
    
    /**
     * Checks if all fields are filled (complete observation)
     * @return true if all measurement fields, observations, and images are present
     */
    public boolean checkComplete() {
        return temperature != null 
               && ph != null 
               && alkalinity != null 
               && turbidity != null
               && observations != null && !observations.trim().isEmpty()
               && imagePaths != null && !imagePaths.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "Observation{" +
                "id='" + id + '\'' +
                ", citizenId='" + citizenId + '\'' +
                ", postcode='" + postcode + '\'' +
                ", temperature=" + temperature +
                ", ph=" + ph +
                ", alkalinity=" + alkalinity +
                ", turbidity=" + turbidity +
                ", observations='" + observations + '\'' +
                ", imagePaths='" + imagePaths + '\'' +
                ", submissionTimestamp=" + submissionTimestamp +
                ", isValid=" + isValid +
                ", isComplete=" + isComplete +
                '}';
    }
}
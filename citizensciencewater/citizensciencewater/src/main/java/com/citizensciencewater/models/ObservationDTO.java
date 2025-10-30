package com.citizensciencewater.models;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Data Transfer Object for receiving water quality observation submissions.
 * This class is used to transfer data from client requests to the service layer.
 * It includes validation annotations to ensure data integrity.
 */
public class ObservationDTO {
    
    @NotBlank(message = "Citizen ID is required")
    private String citizenId;
    
    @NotBlank(message = "Postcode is required")
    private String postcode;
    
    private Double temperature;
    private Double ph;
    private Double alkalinity;
    private Double turbidity;
    private List<String> observations;
    private List<String> imagePaths;
    
    // Constructors
    public ObservationDTO() {
    }
    
    public ObservationDTO(String citizenId, String postcode, Double temperature, Double ph, 
                         Double alkalinity, Double turbidity, List<String> observations, 
                         List<String> imagePaths) {
        this.citizenId = citizenId;
        this.postcode = postcode;
        this.temperature = temperature;
        this.ph = ph;
        this.alkalinity = alkalinity;
        this.turbidity = turbidity;
        this.observations = observations;
        this.imagePaths = imagePaths;
    }
    
    // Getters
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
    
    public List<String> getObservations() {
        return observations;
    }
    
    public List<String> getImagePaths() {
        return imagePaths;
    }
    
    // Setters
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
    
    public void setObservations(List<String> observations) {
        this.observations = observations;
    }
    
    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }
    
    /**
     * Converts this DTO to an Observation entity
     * @return Observation entity with data from this DTO
     */
    public Observation toEntity() {
        Observation observation = new Observation();
        observation.setCitizenId(this.citizenId);
        observation.setPostcode(this.postcode);
        observation.setTemperature(this.temperature);
        observation.setPh(this.ph);
        observation.setAlkalinity(this.alkalinity);
        observation.setTurbidity(this.turbidity);
        
        // Convert list of observations to comma-separated string
        if (this.observations != null && !this.observations.isEmpty()) {
            observation.setObservations(String.join(",", this.observations));
        }
        
        // Convert list of image paths to comma-separated string
        if (this.imagePaths != null && !this.imagePaths.isEmpty()) {
            observation.setImagePaths(String.join(",", this.imagePaths));
        }
        
        return observation;
    }
    
    @Override
    public String toString() {
        return "ObservationDTO{" +
                "citizenId='" + citizenId + '\'' +
                ", postcode='" + postcode + '\'' +
                ", temperature=" + temperature +
                ", ph=" + ph +
                ", alkalinity=" + alkalinity +
                ", turbidity=" + turbidity +
                ", observations=" + observations +
                ", imagePaths=" + imagePaths +
                '}';
    }
}
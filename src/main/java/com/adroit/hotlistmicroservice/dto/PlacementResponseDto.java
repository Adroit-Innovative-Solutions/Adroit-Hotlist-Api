package com.adroit.hotlistmicroservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PlacementResponseDto {
    private String placementId;
    private String candidateFullName;
    private String candidateContactNo;
    @JsonIgnore
    private boolean isPlaced;  // Add isPlaced field

    // Constructors

    public PlacementResponseDto(String placementId, String candidateFullName, String candidateContactNo, boolean isPlaced) {
        this.placementId = placementId;
        this.candidateFullName = candidateFullName;
        this.candidateContactNo = candidateContactNo;
        this.isPlaced = isPlaced;  // Set the value of isPlaced in the constructor
    }

    public PlacementResponseDto(String placemnetId, String candidateFullName, String candidateContactNo) {
        this.placementId = placemnetId;
        this.candidateFullName = candidateFullName;
        this.candidateContactNo = candidateContactNo;
    }
// Getters and Setters


    public String getPlacementId() {
        return placementId;
    }

    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }

    public String getCandidateFullName() {
        return candidateFullName;
    }

    public void setCandidateFullName(String candidateFullName) {
        this.candidateFullName = candidateFullName;
    }

    public String getCandidateContactNo() {
        return candidateContactNo;
    }

    public void setCandidateContactNo(String candidateContactNo) {
        this.candidateContactNo = candidateContactNo;
    }

    @JsonIgnore
    public boolean isPlaced() {
        return isPlaced;  // Getter for isPlaced
    }

    public void setPlaced(boolean isPlaced) {
        this.isPlaced = isPlaced;  // Setter for isPlaced
    }
}

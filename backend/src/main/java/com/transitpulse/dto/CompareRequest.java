package com.transitpulse.dto;

import com.transitpulse.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CompareRequest {

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @Positive
    private Double distanceKm;

    @NotNull
    private Priority priority = Priority.BALANCED;

    private boolean avoidParking;
    private Double maxHours;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isAvoidParking() {
        return avoidParking;
    }

    public void setAvoidParking(boolean avoidParking) {
        this.avoidParking = avoidParking;
    }

    public Double getMaxHours() {
        return maxHours;
    }

    public void setMaxHours(Double maxHours) {
        this.maxHours = maxHours;
    }
}

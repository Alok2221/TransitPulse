package com.transitpulse.dto;

import com.transitpulse.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TripQueryRequest {

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @Positive
    private double distanceKm;

    @NotNull
    private Priority priority = Priority.BALANCED;

    private String notes;
    private String resultSnapshotJson;

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

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getResultSnapshotJson() {
        return resultSnapshotJson;
    }

    public void setResultSnapshotJson(String resultSnapshotJson) {
        this.resultSnapshotJson = resultSnapshotJson;
    }
}

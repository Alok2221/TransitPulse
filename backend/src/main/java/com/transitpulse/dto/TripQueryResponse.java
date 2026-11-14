package com.transitpulse.dto;

import com.transitpulse.domain.Priority;
import com.transitpulse.domain.TripQuery;

import java.time.Instant;

public class TripQueryResponse {

    private Long id;
    private String origin;
    private String destination;
    private double distanceKm;
    private Priority priority;
    private String notes;
    private String resultSnapshotJson;
    private Instant createdAt;

    public static TripQueryResponse from(TripQuery entity) {
        TripQueryResponse dto = new TripQueryResponse();
        dto.id = entity.getId();
        dto.origin = entity.getOrigin();
        dto.destination = entity.getDestination();
        dto.distanceKm = entity.getDistanceKm();
        dto.priority = entity.getPriority();
        dto.notes = entity.getNotes();
        dto.resultSnapshotJson = entity.getResultSnapshotJson();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getNotes() {
        return notes;
    }

    public String getResultSnapshotJson() {
        return resultSnapshotJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

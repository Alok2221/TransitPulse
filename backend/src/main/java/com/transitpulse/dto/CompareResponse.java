package com.transitpulse.dto;

import com.transitpulse.domain.Priority;
import com.transitpulse.domain.TransportMode;

import java.util.ArrayList;
import java.util.List;

public class CompareResponse {

    private String origin;
    private String destination;
    private double distanceKm;
    private Priority priority;
    private TransportMode recommendedMode;
    private RouteMetaDto meta = new RouteMetaDto();
    private List<ModeOptionDto> ranking = new ArrayList<>();

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

    public TransportMode getRecommendedMode() {
        return recommendedMode;
    }

    public void setRecommendedMode(TransportMode recommendedMode) {
        this.recommendedMode = recommendedMode;
    }

    public RouteMetaDto getMeta() {
        return meta;
    }

    public void setMeta(RouteMetaDto meta) {
        this.meta = meta;
    }

    public List<ModeOptionDto> getRanking() {
        return ranking;
    }

    public void setRanking(List<ModeOptionDto> ranking) {
        this.ranking = ranking;
    }
}

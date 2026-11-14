package com.transitpulse.dto;

import com.transitpulse.domain.Priority;
import com.transitpulse.domain.TransportMode;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgentAdviseResponse {

    private String recommendation;
    private String reasoning;
    private TransportMode recommendedMode;
    private Priority detectedPriority;
    private Map<String, Object> parsedConstraints = new LinkedHashMap<>();
    private WeatherDto weather;
    private CompareResponse comparison;

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public TransportMode getRecommendedMode() {
        return recommendedMode;
    }

    public void setRecommendedMode(TransportMode recommendedMode) {
        this.recommendedMode = recommendedMode;
    }

    public Priority getDetectedPriority() {
        return detectedPriority;
    }

    public void setDetectedPriority(Priority detectedPriority) {
        this.detectedPriority = detectedPriority;
    }

    public Map<String, Object> getParsedConstraints() {
        return parsedConstraints;
    }

    public void setParsedConstraints(Map<String, Object> parsedConstraints) {
        this.parsedConstraints = parsedConstraints;
    }

    public WeatherDto getWeather() {
        return weather;
    }

    public void setWeather(WeatherDto weather) {
        this.weather = weather;
    }

    public CompareResponse getComparison() {
        return comparison;
    }

    public void setComparison(CompareResponse comparison) {
        this.comparison = comparison;
    }
}

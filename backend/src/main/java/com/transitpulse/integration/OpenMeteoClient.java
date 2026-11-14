package com.transitpulse.integration;

import com.transitpulse.domain.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    private final RestClient restClient;

    public OpenMeteoClient(
            RestClient.Builder restClientBuilder,
            @Value("${transitpulse.open-meteo.base-url:https://api.open-meteo.com}") String baseUrl
    ) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restClient = restClientBuilder
                .baseUrl(normalized)
                .defaultHeader("User-Agent", "TransitPulseMVP/1.0 (recruitment; local)")
                .build();
    }

    @SuppressWarnings("unchecked")
    public Optional<OpenMeteoSnapshot> current(GeoPoint point) {
        try {
            Map<String, Object> body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", point.lat())
                            .queryParam("longitude", point.lon())
                            .queryParam("current", "temperature_2m,weather_code,wind_speed_10m")
                            .queryParam("timezone", "Europe/Warsaw")
                            .build())
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.get("current") == null) {
                return Optional.empty();
            }
            Map<String, Object> current = (Map<String, Object>) body.get("current");
            double temperature = ((Number) current.get("temperature_2m")).doubleValue();
            int weatherCode = ((Number) current.get("weather_code")).intValue();
            double wind = ((Number) current.get("wind_speed_10m")).doubleValue();
            return Optional.of(new OpenMeteoSnapshot(temperature, weatherCode, wind));
        } catch (Exception ex) {
            log.warn("Open-Meteo unavailable: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public record OpenMeteoSnapshot(double temperatureC, int weatherCode, double windSpeedKmh) {
    }
}

package com.transitpulse.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class OpenMeteoGeocodingClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoGeocodingClient.class);

    private final RestClient restClient;

    public OpenMeteoGeocodingClient(
            RestClient.Builder restClientBuilder,
            @Value("${transitpulse.open-meteo.geocoding-url:https://geocoding-api.open-meteo.com}") String baseUrl
    ) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restClient = restClientBuilder
                .baseUrl(normalized)
                .defaultHeader("User-Agent", "TransitPulseMVP/1.0 (recruitment; local)")
                .build();
    }

    @SuppressWarnings("unchecked")
    public List<GeocodedPlace> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        try {
            Map<String, Object> body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search")
                            .queryParam("name", query.trim())
                            .queryParam("count", Math.min(Math.max(limit, 1), 10))
                            .queryParam("language", "pl")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(Map.class);
            if (body == null || body.get("results") == null) {
                return List.of();
            }
            List<Map<String, Object>> results = (List<Map<String, Object>>) body.get("results");
            List<GeocodedPlace> places = new ArrayList<>();
            for (Map<String, Object> row : results) {
                String name = String.valueOf(row.get("name"));
                String admin1 = row.get("admin1") == null ? null : String.valueOf(row.get("admin1"));
                String country = row.get("country") == null ? null : String.valueOf(row.get("country"));
                String countryCode = row.get("country_code") == null ? null : String.valueOf(row.get("country_code"));
                double lat = ((Number) row.get("latitude")).doubleValue();
                double lon = ((Number) row.get("longitude")).doubleValue();
                places.add(new GeocodedPlace(name, admin1, country, countryCode, lat, lon));
            }
            return places;
        } catch (Exception ex) {
            log.warn("Open-Meteo geocoding unavailable: {}", ex.getMessage());
            return List.of();
        }
    }

    public record GeocodedPlace(
            String name,
            String admin1,
            String country,
            String countryCode,
            double lat,
            double lon
    ) {
        public String label() {
            StringBuilder sb = new StringBuilder(name);
            if (admin1 != null && !admin1.isBlank() && !admin1.equalsIgnoreCase(name)) {
                sb.append(", ").append(admin1);
            }
            if (country != null && !country.isBlank()) {
                sb.append(", ").append(country);
            }
            return sb.toString();
        }
    }
}

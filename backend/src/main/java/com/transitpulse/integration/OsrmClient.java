package com.transitpulse.integration;

import com.transitpulse.domain.GeoPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OsrmClient {

    private static final Logger log = LoggerFactory.getLogger(OsrmClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public OsrmClient(
            RestClient.Builder restClientBuilder,
            @Value("${transitpulse.osrm.base-url:https://router.project-osrm.org}") String baseUrl
    ) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restClient = restClientBuilder
                .baseUrl(this.baseUrl)
                .defaultHeader("User-Agent", "TransitPulseMVP/1.0 (recruitment; local)")
                .build();
    }

    @SuppressWarnings("unchecked")
    public Optional<OsrmRoute> drivingRoute(GeoPoint from, GeoPoint to) {
        String path = String.format(
                "/route/v1/driving/%s,%s;%s,%s?overview=false&alternatives=false",
                from.lon(), from.lat(), to.lon(), to.lat()
        );
        try {
            Map<String, Object> body = restClient.get()
                    .uri(path)
                    .retrieve()
                    .body(Map.class);
            if (body == null || !"Ok".equals(String.valueOf(body.get("code")))) {
                return Optional.empty();
            }
            List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
            if (routes == null || routes.isEmpty()) {
                return Optional.empty();
            }
            Map<String, Object> route = routes.get(0);
            double meters = ((Number) route.get("distance")).doubleValue();
            double seconds = ((Number) route.get("duration")).doubleValue();
            return Optional.of(new OsrmRoute(meters / 1000.0, seconds / 60.0));
        } catch (Exception ex) {
            log.warn("OSRM unavailable: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public record OsrmRoute(double distanceKm, double durationMinutes) {
    }
}

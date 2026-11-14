package com.transitpulse.service;

import com.transitpulse.domain.GeoPoint;
import com.transitpulse.dto.CitySuggestionDto;
import com.transitpulse.integration.OpenMeteoGeocodingClient;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CityDirectoryService {

    private final CityDistanceService cityDistanceService;
    private final OpenMeteoGeocodingClient geocodingClient;
    private final Map<String, ResolvedPlace> cache = new ConcurrentHashMap<>();

    public CityDirectoryService(CityDistanceService cityDistanceService, OpenMeteoGeocodingClient geocodingClient) {
        this.cityDistanceService = cityDistanceService;
        this.geocodingClient = geocodingClient;
    }

    public List<CitySuggestionDto> suggest(String query, int limit) {
        String q = query == null ? "" : query.trim();
        if (q.length() < 2) {
            return List.of();
        }

        List<CitySuggestionDto> suggestions = new ArrayList<>();
        String folded = fold(q);

        for (String city : cityDistanceService.knownCities()) {
            if (fold(city).startsWith(folded) || fold(city).contains(folded)) {
                GeoPoint point = cityDistanceService.coordinatesOf(city).orElse(null);
                if (point != null) {
                    suggestions.add(new CitySuggestionDto(
                            city,
                            city + ", Polska",
                            "Polska",
                            "PL",
                            point.lat(),
                            point.lon()
                    ));
                }
            }
            if (suggestions.size() >= limit) {
                return suggestions;
            }
        }

        List<OpenMeteoGeocodingClient.GeocodedPlace> remote = geocodingClient.search(q, Math.max(limit * 2, 8));
        remote.sort((a, b) -> {
            int scoreA = scorePlace(a, folded);
            int scoreB = scorePlace(b, folded);
            return Integer.compare(scoreB, scoreA);
        });

        for (OpenMeteoGeocodingClient.GeocodedPlace place : remote) {
            boolean duplicate = suggestions.stream().anyMatch(s ->
                    fold(s.getName()).equals(fold(place.name()))
                            && Math.abs(s.getLat() - place.lat()) < 0.05
            );
            if (!duplicate) {
                suggestions.add(new CitySuggestionDto(
                        place.name(),
                        place.label(),
                        place.country(),
                        place.countryCode(),
                        place.lat(),
                        place.lon()
                ));
            }
            if (suggestions.size() >= limit) {
                break;
            }
        }
        return suggestions;
    }

    private int scorePlace(OpenMeteoGeocodingClient.GeocodedPlace place, String foldedQuery) {
        int score = 0;
        if ("PL".equalsIgnoreCase(place.countryCode())) {
            score += 100;
        }
        String foldedName = fold(place.name());
        if (foldedName.equals(foldedQuery)) {
            score += 50;
        } else if (foldedName.startsWith(foldedQuery)) {
            score += 30;
        } else if (foldedName.contains(foldedQuery)) {
            score += 10;
        }
        return score;
    }

    public ResolvedPlace resolve(String raw) {
        String normalized = cityDistanceService.normalizeCity(raw);
        String cacheKey = fold(normalized);

        ResolvedPlace cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Optional<GeoPoint> seed = cityDistanceService.coordinatesOf(normalized);
        if (seed.isPresent()) {
            ResolvedPlace place = new ResolvedPlace(normalized, seed.get(), "seed");
            cache.put(cacheKey, place);
            return place;
        }

        List<OpenMeteoGeocodingClient.GeocodedPlace> hits = geocodingClient.search(normalized, 5);
        OpenMeteoGeocodingClient.GeocodedPlace best = pickBest(hits, normalized);
        if (best != null) {
            ResolvedPlace place = new ResolvedPlace(
                    best.name(),
                    new GeoPoint(best.lat(), best.lon()),
                    "geocoding"
            );
            cache.put(cacheKey, place);
            cache.put(fold(best.name()), place);
            return place;
        }

        ResolvedPlace unknown = new ResolvedPlace(normalized, null, "unknown");
        cache.put(cacheKey, unknown);
        return unknown;
    }

    private OpenMeteoGeocodingClient.GeocodedPlace pickBest(
            List<OpenMeteoGeocodingClient.GeocodedPlace> hits,
            String query
    ) {
        if (hits.isEmpty()) {
            return null;
        }
        String foldedQuery = fold(query);
        return hits.stream()
                .filter(p -> "PL".equalsIgnoreCase(p.countryCode()))
                .filter(p -> fold(p.name()).equals(foldedQuery) || fold(p.name()).startsWith(foldedQuery))
                .findFirst()
                .or(() -> hits.stream().filter(p -> "PL".equalsIgnoreCase(p.countryCode())).findFirst())
                .orElse(hits.get(0));
    }

    private String fold(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    public record ResolvedPlace(String name, GeoPoint point, String source) {
        public boolean hasPoint() {
            return point != null;
        }
    }
}

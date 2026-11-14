package com.transitpulse.service;

import com.transitpulse.domain.GeoPoint;
import com.transitpulse.integration.OsrmClient;
import org.springframework.stereotype.Service;

@Service
public class RouteResolverService {

    private static final double ROAD_FACTOR = 1.30;
    private static final double FALLBACK_CAR_KMH = 85.0;

    private final CityDistanceService cityDistanceService;
    private final CityDirectoryService cityDirectoryService;
    private final OsrmClient osrmClient;

    public RouteResolverService(
            CityDistanceService cityDistanceService,
            CityDirectoryService cityDirectoryService,
            OsrmClient osrmClient
    ) {
        this.cityDistanceService = cityDistanceService;
        this.cityDirectoryService = cityDirectoryService;
        this.osrmClient = osrmClient;
    }

    public ResolvedRoute resolve(String origin, String destination, Double providedKm) {
        CityDirectoryService.ResolvedPlace from = cityDirectoryService.resolve(origin);
        CityDirectoryService.ResolvedPlace to = cityDirectoryService.resolve(destination);

        if (providedKm != null && providedKm > 0) {
            return new ResolvedRoute(from.name(), to.name(), providedKm, null, "manual");
        }

        if (from.hasPoint() && to.hasPoint()) {
            var osrm = osrmClient.drivingRoute(from.point(), to.point());
            if (osrm.isPresent()) {
                OsrmClient.OsrmRoute route = osrm.get();
                return new ResolvedRoute(
                        from.name(),
                        to.name(),
                        round1(route.distanceKm()),
                        round1(route.durationMinutes()),
                        "osrm"
                );
            }

            double straight = haversineKm(from.point(), to.point());
            double roadKm = round1(straight * ROAD_FACTOR);
            double minutes = round1((roadKm / FALLBACK_CAR_KMH) * 60.0);
            return new ResolvedRoute(from.name(), to.name(), roadKm, minutes, "haversine");
        }

        Double seeded = cityDistanceService.lookup(from.name(), to.name()).orElse(null);
        if (seeded != null) {
            return new ResolvedRoute(from.name(), to.name(), seeded, null, "seed");
        }

        return new ResolvedRoute(from.name(), to.name(), 120.0, null, "default");
    }

    private double haversineKm(GeoPoint a, GeoPoint b) {
        double earthRadius = 6371.0;
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLon = Math.toRadians(b.lon() - a.lon());
        double lat1 = Math.toRadians(a.lat());
        double lat2 = Math.toRadians(b.lat());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * earthRadius * Math.asin(Math.sqrt(h));
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public record ResolvedRoute(
            String origin,
            String destination,
            double distanceKm,
            Double carDurationMinutes,
            String distanceSource
    ) {
    }
}

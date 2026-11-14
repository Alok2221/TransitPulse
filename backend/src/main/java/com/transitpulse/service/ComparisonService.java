package com.transitpulse.service;

import com.transitpulse.domain.Priority;
import com.transitpulse.domain.TransportMode;
import com.transitpulse.dto.CompareRequest;
import com.transitpulse.dto.CompareResponse;
import com.transitpulse.dto.ModeOptionDto;
import com.transitpulse.dto.RouteMetaDto;
import com.transitpulse.dto.WeatherDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ComparisonService {

    private final double fuelPlnPerLiter;
    private final double carLPer100Km;
    private final double carAmortizationPerKm;
    private final double parkingPln;

    private static final double TRAIN_PLN_PER_KM = 0.42;
    private static final double BUS_PLN_PER_KM = 0.28;

    private static final double CAR_CO2_G_PER_KM = 170;
    private static final double TRAIN_CO2_G_PER_KM = 40;
    private static final double BUS_CO2_G_PER_KM = 90;
    private static final double BIKE_CO2_G_PER_KM = 0;

    private static final double CAR_KMH = 85;
    private static final double TRAIN_KMH = 95;
    private static final double BUS_KMH = 65;
    private static final double BIKE_KMH = 18;

    private final RouteResolverService routeResolverService;

    public ComparisonService(
            RouteResolverService routeResolverService,
            @Value("${transitpulse.fuel.pln-per-liter:6.55}") double fuelPlnPerLiter,
            @Value("${transitpulse.fuel.liters-per-100km:7.0}") double carLPer100Km,
            @Value("${transitpulse.car.amortization-per-km:0.35}") double carAmortizationPerKm,
            @Value("${transitpulse.car.parking-pln:25.0}") double parkingPln
    ) {
        this.routeResolverService = routeResolverService;
        this.fuelPlnPerLiter = fuelPlnPerLiter;
        this.carLPer100Km = carLPer100Km;
        this.carAmortizationPerKm = carAmortizationPerKm;
        this.parkingPln = parkingPln;
    }

    public CompareResponse compare(CompareRequest request) {
        return compare(request, null, null);
    }

    public CompareResponse compare(CompareRequest request, WeatherDto weather) {
        return compare(request, weather, null);
    }

    public CompareResponse compare(CompareRequest request, WeatherDto weather, String weatherSource) {
        RouteResolverService.ResolvedRoute route = routeResolverService.resolve(
                request.getOrigin(),
                request.getDestination(),
                request.getDistanceKm()
        );
        Priority priority = request.getPriority() == null ? Priority.BALANCED : request.getPriority();
        double distanceKm = route.distanceKm();

        List<ModeOptionDto> options = new ArrayList<>();
        options.add(buildCar(distanceKm, request.isAvoidParking(), route.carDurationMinutes(), route.distanceSource()));
        options.add(buildTrain(distanceKm));
        options.add(buildBus(distanceKm));
        options.add(buildBike(distanceKm, weather));

        applyDeadline(options, request.getMaxHours());
        scoreAndRank(options, priority);

        RouteMetaDto meta = new RouteMetaDto();
        meta.setDistanceSource(route.distanceSource());
        meta.setWeatherSource(weatherSource);
        meta.setCarDurationMinutesFromOsrm(route.carDurationMinutes());

        CompareResponse response = new CompareResponse();
        response.setOrigin(route.origin());
        response.setDestination(route.destination());
        response.setDistanceKm(round1(distanceKm));
        response.setPriority(priority);
        response.setMeta(meta);
        response.setRanking(options);
        response.setRecommendedMode(options.isEmpty() ? null : options.get(0).getMode());
        return response;
    }

    private ModeOptionDto buildCar(double km, boolean avoidParking, Double osrmMinutes, String distanceSource) {
        double fuel = (km / 100.0) * carLPer100Km * fuelPlnPerLiter;
        double amortization = km * carAmortizationPerKm;
        double parking = avoidParking ? 0 : parkingPln;
        ModeOptionDto dto = base(TransportMode.CAR, "Samochód");
        dto.setCostPln(round2(fuel + amortization + parking));
        if (osrmMinutes != null && osrmMinutes > 0) {
            dto.setDurationMinutes(round1(osrmMinutes));
        } else {
            dto.setDurationMinutes(round1((km / CAR_KMH) * 60.0));
        }
        dto.setCo2Kg(round2((CAR_CO2_G_PER_KM * km) / 1000.0));
        dto.setFeasible(true);
        String parkingNote = avoidParking
                ? "Paliwo + amortyzacja, bez parkingu"
                : "Paliwo + amortyzacja + parking ok. " + (int) parkingPln + " zł";
        String sourceNote = "osrm".equals(distanceSource)
                ? " · czas z mapy drogowej"
                : " · czas szacunkowy";
        dto.setNote(parkingNote + sourceNote);
        return dto;
    }

    private ModeOptionDto buildTrain(double km) {
        ModeOptionDto dto = base(TransportMode.TRAIN, "Pociąg");
        dto.setCostPln(round2(Math.max(12, km * TRAIN_PLN_PER_KM)));
        dto.setDurationMinutes(round1((km / TRAIN_KMH) * 60.0 + 20));
        dto.setCo2Kg(round2((TRAIN_CO2_G_PER_KM * km) / 1000.0));
        dto.setFeasible(true);
        dto.setNote("Szacunkowy bilet + ok. 20 min na dojście");
        return dto;
    }

    private ModeOptionDto buildBus(double km) {
        ModeOptionDto dto = base(TransportMode.BUS, "Autobus");
        dto.setCostPln(round2(Math.max(10, km * BUS_PLN_PER_KM)));
        dto.setDurationMinutes(round1((km / BUS_KMH) * 60.0 + 15));
        dto.setCo2Kg(round2((BUS_CO2_G_PER_KM * km) / 1000.0));
        dto.setFeasible(true);
        dto.setNote("Szacunkowy bilet dalekobieżny");
        return dto;
    }

    private ModeOptionDto buildBike(double km, WeatherDto weather) {
        ModeOptionDto dto = base(TransportMode.BIKE, "Rower");
        dto.setCostPln(0);
        dto.setDurationMinutes(round1((km / BIKE_KMH) * 60.0));
        dto.setCo2Kg(BIKE_CO2_G_PER_KM);
        boolean weatherOk = weather == null || weather.isBikeFriendly();
        if (km > 80) {
            dto.setFeasible(false);
            dto.setNote("Dystans > 80 km — rower niezalecany");
        } else if (!weatherOk) {
            dto.setFeasible(false);
            dto.setNote("Pogoda niesprzyjająca jeździe rowerem");
        } else {
            dto.setFeasible(true);
            dto.setNote("Zero kosztów i emisji, wymaga sprawności");
        }
        return dto;
    }

    private void applyDeadline(List<ModeOptionDto> options, Double maxHours) {
        if (maxHours == null || maxHours <= 0) {
            return;
        }
        double maxMinutes = maxHours * 60.0;
        for (ModeOptionDto option : options) {
            if (option.getDurationMinutes() > maxMinutes) {
                option.setFeasible(false);
                String existing = option.getNote() == null ? "" : option.getNote() + " · ";
                option.setNote(existing + "Przekracza limit " + round1(maxHours) + " h");
            }
        }
    }

    private void scoreAndRank(List<ModeOptionDto> options, Priority priority) {
        double minCost = options.stream().mapToDouble(ModeOptionDto::getCostPln).min().orElse(1);
        double maxCost = options.stream().mapToDouble(ModeOptionDto::getCostPln).max().orElse(1);
        double minTime = options.stream().mapToDouble(ModeOptionDto::getDurationMinutes).min().orElse(1);
        double maxTime = options.stream().mapToDouble(ModeOptionDto::getDurationMinutes).max().orElse(1);
        double minCo2 = options.stream().mapToDouble(ModeOptionDto::getCo2Kg).min().orElse(0);
        double maxCo2 = options.stream().mapToDouble(ModeOptionDto::getCo2Kg).max().orElse(1);

        Map<Priority, double[]> weights = new EnumMap<>(Priority.class);
        weights.put(Priority.CHEAPEST, new double[]{0.65, 0.15, 0.20});
        weights.put(Priority.FASTEST, new double[]{0.15, 0.70, 0.15});
        weights.put(Priority.GREENEST, new double[]{0.15, 0.15, 0.70});
        weights.put(Priority.BALANCED, new double[]{0.34, 0.33, 0.33});
        double[] w = weights.get(priority);

        for (ModeOptionDto option : options) {
            double costScore = normalizeInverse(option.getCostPln(), minCost, maxCost);
            double timeScore = normalizeInverse(option.getDurationMinutes(), minTime, maxTime);
            double co2Score = normalizeInverse(option.getCo2Kg(), minCo2, maxCo2);
            double score = w[0] * costScore + w[1] * timeScore + w[2] * co2Score;
            if (!option.isFeasible()) {
                score *= 0.35;
            }
            option.setScore(round2(score * 100));
        }

        options.sort(Comparator.comparingDouble(ModeOptionDto::getScore).reversed());
        for (int i = 0; i < options.size(); i++) {
            options.get(i).setRank(i + 1);
            options.get(i).setRecommended(i == 0);
        }
    }

    private ModeOptionDto base(TransportMode mode, String label) {
        ModeOptionDto dto = new ModeOptionDto();
        dto.setMode(mode);
        dto.setLabel(label);
        return dto;
    }

    private double normalizeInverse(double value, double min, double max) {
        if (Math.abs(max - min) < 1e-9) {
            return 1.0;
        }
        return 1.0 - ((value - min) / (max - min));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}

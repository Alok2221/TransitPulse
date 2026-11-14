package com.transitpulse.service;

import com.transitpulse.domain.GeoPoint;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class CityDistanceService {

    private final Map<String, Double> distances = new LinkedHashMap<>();
    private final Map<String, GeoPoint> coordinates = new LinkedHashMap<>();

    public CityDistanceService() {
        putCoords("Katowice", 50.2649, 19.0238);
        putCoords("Kraków", 50.0647, 19.9450);
        putCoords("Warszawa", 52.2297, 21.0122);
        putCoords("Wrocław", 51.1079, 17.0385);
        putCoords("Poznań", 52.4064, 16.9252);
        putCoords("Gdańsk", 54.3520, 18.6466);
        putCoords("Gdynia", 54.5189, 18.5305);
        putCoords("Łódź", 51.7592, 19.4560);
        putCoords("Lublin", 51.2465, 22.5684);
        putCoords("Rzeszów", 50.0413, 21.9990);
        putCoords("Bydgoszcz", 53.1235, 18.0084);
        putCoords("Toruń", 53.0138, 18.5984);
        putCoords("Szczecin", 53.4285, 14.5528);
        putCoords("Gliwice", 50.2945, 18.6714);

        put("Katowice", "Kraków", 80);
        put("Katowice", "Warszawa", 295);
        put("Katowice", "Wrocław", 190);
        put("Katowice", "Gliwice", 28);
        put("Kraków", "Warszawa", 290);
        put("Kraków", "Wrocław", 270);
        put("Kraków", "Gdańsk", 600);
        put("Warszawa", "Łódź", 135);
        put("Warszawa", "Gdańsk", 340);
        put("Warszawa", "Poznań", 310);
        put("Warszawa", "Wrocław", 350);
        put("Wrocław", "Poznań", 180);
        put("Poznań", "Gdańsk", 300);
        put("Gdańsk", "Gdynia", 22);
        put("Lublin", "Warszawa", 170);
        put("Rzeszów", "Kraków", 160);
        put("Bydgoszcz", "Toruń", 50);
        put("Szczecin", "Poznań", 230);
    }

    public Optional<GeoPoint> coordinatesOf(String city) {
        return Optional.ofNullable(coordinates.get(normalizeCity(city)));
    }

    public java.util.Set<String> knownCities() {
        return java.util.Collections.unmodifiableSet(coordinates.keySet());
    }

    public Optional<Double> lookup(String origin, String destination) {
        String key = key(origin, destination);
        Double value = distances.get(key);
        if (value != null) {
            return Optional.of(value);
        }
        return Optional.ofNullable(distances.get(key(destination, origin)));
    }

    public double resolveDistance(String origin, String destination, Double provided) {
        if (provided != null && provided > 0) {
            return provided;
        }
        return lookup(origin, destination).orElse(120.0);
    }

    public String normalizeCity(String raw) {
        if (raw == null) {
            return "";
        }
        String trimmed = raw.trim();
        String ascii = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return switch (ascii) {
            case "krakow", "cracow", "krakowie", "krakowa" -> "Kraków";
            case "lodz", "lodzi" -> "Łódź";
            case "gdansk", "gdansku", "gdanska" -> "Gdańsk";
            case "wroclaw", "wroclawiu", "wroclawia" -> "Wrocław";
            case "poznan", "poznaniu", "poznania" -> "Poznań";
            case "rzeszow", "rzeszowie" -> "Rzeszów";
            case "katowic", "katowicach" -> "Katowice";
            case "warszawie", "warszawy" -> "Warszawa";
            case "gliwic", "gliwicach" -> "Gliwice";
            default -> {
                for (String known : coordinates.keySet()) {
                    if (fold(known).equals(ascii)) {
                        yield known;
                    }
                }
                yield capitalize(trimmed);
            }
        };
    }

    private void putCoords(String city, double lat, double lon) {
        coordinates.put(city, new GeoPoint(lat, lon));
    }

    private void put(String a, String b, double km) {
        distances.put(key(a, b), km);
    }

    private String key(String a, String b) {
        return normalizeCity(a).toLowerCase(Locale.ROOT) + "|" + normalizeCity(b).toLowerCase(Locale.ROOT);
    }

    private String fold(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private String capitalize(String value) {
        if (value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}

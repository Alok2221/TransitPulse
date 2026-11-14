package com.transitpulse.service;

import com.transitpulse.domain.Priority;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NlPreferenceParser {

    private static final List<String> CITIES = List.of(
            "Katowice", "Kraków", "Warszawa", "Wrocław", "Poznań", "Gdańsk",
            "Gdynia", "Łódź", "Lublin", "Rzeszów", "Bydgoszcz", "Toruń",
            "Szczecin", "Gliwice"
    );

    private static final Pattern HOURS = Pattern.compile(
            "(\\d+(?:[.,]\\d+)?)\\s*h(?:odz(?:iny|in)?)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IN_X_FROM_Y = Pattern.compile(
            "\\bw\\s+([A-Za-zĄąĆćĘęŁłŃńÓóŚśŹźŻż]+)\\s+z\\s+([A-Za-zĄąĆćĘęŁłŃńÓóŚśŹźŻż]+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Pattern FROM_TO = Pattern.compile(
            "(?:\\bz\\s+|\\bze\\s+)([A-Za-zĄąĆćĘęŁłŃńÓóŚśŹźŻż]+)\\s+(?:do|→|->)\\s+([A-Za-zĄąĆćĘęŁłŃńÓóŚśŹźŻż]+)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private final CityDistanceService cityDistanceService;

    public NlPreferenceParser(CityDistanceService cityDistanceService) {
        this.cityDistanceService = cityDistanceService;
    }

    public ParsedTrip parse(String message) {
        String text = message == null ? "" : message.trim();
        String lower = fold(text);

        String origin = null;
        String destination = null;

        Matcher inFrom = IN_X_FROM_Y.matcher(text);
        if (inFrom.find()) {
            destination = resolveCityToken(inFrom.group(1));
            origin = resolveCityToken(inFrom.group(2));
        }

        if (origin == null || destination == null) {
            Matcher route = FROM_TO.matcher(text);
            if (route.find()) {
                origin = resolveCityToken(route.group(1));
                destination = resolveCityToken(route.group(2));
            }
        }

        List<String> mentioned = findCities(text);
        if ((origin == null || destination == null) && mentioned.size() >= 2) {
            if (lower.contains("byc w") || lower.contains("w ")) {
                destination = mentioned.get(0);
                origin = mentioned.get(1);
            } else {
                origin = mentioned.get(0);
                destination = mentioned.get(1);
            }
        } else if ((origin == null || destination == null) && mentioned.size() == 1) {
            destination = mentioned.get(0);
            origin = "Katowice";
        }

        if (origin == null) {
            origin = "Katowice";
        }
        if (destination == null) {
            destination = "Kraków";
        }

        Priority priority = Priority.BALANCED;
        if (containsAny(lower, "najtaniej", "tanio", "tani", "budzet", "nie chce placic", "nie chce placic za")) {
            priority = Priority.CHEAPEST;
        }
        if (containsAny(lower, "najszybciej", "szybko", "pilne", "na czas", "musze byc", "mam 1h", "mam 2h", "mam 3h")) {
            priority = Priority.FASTEST;
        }
        if (containsAny(lower, "eko", "zielon", "co2", "emisj", "srodowisk")) {
            priority = Priority.GREENEST;
        }

        boolean avoidParking = containsAny(lower, "parking", "bez parkingu");
        if (avoidParking && containsAny(lower, "nie chce placic", "nie placic") && priority == Priority.BALANCED) {
            priority = Priority.CHEAPEST;
        }

        Double maxHours = null;
        Matcher hours = HOURS.matcher(lower);
        if (hours.find()) {
            maxHours = Double.parseDouble(hours.group(1).replace(',', '.'));
            if (priority == Priority.BALANCED || priority == Priority.CHEAPEST) {
                priority = Priority.FASTEST;
            }
        }

        boolean morning = containsAny(lower, "rano", "jutro rano", "poranek");

        Map<String, Object> constraints = new LinkedHashMap<>();
        constraints.put("origin", origin);
        constraints.put("destination", destination);
        constraints.put("priority", priority.name());
        constraints.put("avoidParking", avoidParking);
        constraints.put("maxHours", maxHours);
        constraints.put("morning", morning);
        constraints.put("raw", text);

        ParsedTrip parsed = new ParsedTrip();
        parsed.origin = origin;
        parsed.destination = destination;
        parsed.priority = priority;
        parsed.avoidParking = avoidParking;
        parsed.maxHours = maxHours;
        parsed.morning = morning;
        parsed.constraints = constraints;
        return parsed;
    }

    private String resolveCityToken(String token) {
        String folded = fold(token);
        // locative / genitive PL forms → nominative
        Map<String, String> forms = Map.ofEntries(
                Map.entry("krakowie", "Kraków"),
                Map.entry("krakowa", "Kraków"),
                Map.entry("krakow", "Kraków"),
                Map.entry("katowic", "Katowice"),
                Map.entry("katowicach", "Katowice"),
                Map.entry("warszawie", "Warszawa"),
                Map.entry("warszawy", "Warszawa"),
                Map.entry("wroclawiu", "Wrocław"),
                Map.entry("wroclawia", "Wrocław"),
                Map.entry("poznaniu", "Poznań"),
                Map.entry("poznania", "Poznań"),
                Map.entry("gdansku", "Gdańsk"),
                Map.entry("gdanska", "Gdańsk"),
                Map.entry("lodzi", "Łódź"),
                Map.entry("lublinie", "Lublin"),
                Map.entry("rzeszowie", "Rzeszów"),
                Map.entry("szczecinie", "Szczecin"),
                Map.entry("gliwicach", "Gliwice"),
                Map.entry("gliwic", "Gliwice")
        );
        if (forms.containsKey(folded)) {
            return forms.get(folded);
        }
        return cityDistanceService.normalizeCity(token);
    }

    private List<String> findCities(String text) {
        List<String> found = new ArrayList<>();
        String foldedText = fold(text);
        for (String city : CITIES) {
            String foldedCity = fold(city);
            String ascii = foldedCity.replace("ł", "l").replace("ó", "o");
            boolean hit = foldedText.contains(foldedCity)
                    || foldedText.contains(ascii)
                    || foldedText.contains(foldedCity + "ie")
                    || foldedText.contains(foldedCity + "u")
                    || foldedText.contains(ascii + "ie")
                    || (foldedCity.endsWith("e") && foldedText.contains(foldedCity.substring(0, foldedCity.length() - 1) + "ach"))
                    || (foldedCity.equals("katowice") && (foldedText.contains("katowic") || foldedText.contains("katowicach")))
                    || (foldedCity.equals("krakow") && (foldedText.contains("krakowie") || foldedText.contains("krakowa")));
            if (hit) {
                String normalized = cityDistanceService.normalizeCity(city);
                if (found.stream().noneMatch(c -> fold(c).equals(fold(normalized)))) {
                    found.add(normalized);
                }
            }
        }
        return found;
    }

    private boolean containsAny(String haystack, String... needles) {
        for (String needle : needles) {
            if (haystack.contains(fold(needle))) {
                return true;
            }
        }
        return false;
    }

    private String fold(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public static class ParsedTrip {
        public String origin;
        public String destination;
        public Priority priority;
        public boolean avoidParking;
        public Double maxHours;
        public boolean morning;
        public Map<String, Object> constraints;
    }
}

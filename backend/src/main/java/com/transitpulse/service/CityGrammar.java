package com.transitpulse.service;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CityGrammar {

    private static final Map<String, String> LOCATIVE = Map.ofEntries(
            Map.entry("Katowice", "Katowicach"),
            Map.entry("Kraków", "Krakowie"),
            Map.entry("Warszawa", "Warszawie"),
            Map.entry("Wrocław", "Wrocławiu"),
            Map.entry("Poznań", "Poznaniu"),
            Map.entry("Gdańsk", "Gdańsku"),
            Map.entry("Gdynia", "Gdyni"),
            Map.entry("Łódź", "Łodzi"),
            Map.entry("Lublin", "Lublinie"),
            Map.entry("Rzeszów", "Rzeszowie"),
            Map.entry("Bydgoszcz", "Bydgoszczy"),
            Map.entry("Toruń", "Toruniu"),
            Map.entry("Szczecin", "Szczecinie"),
            Map.entry("Gliwice", "Gliwicach")
    );

    public String inCity(String city) {
        if (city == null || city.isBlank()) {
            return "celu";
        }
        return LOCATIVE.getOrDefault(city, city);
    }
}

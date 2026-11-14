package com.transitpulse.service;

import com.transitpulse.dto.WeatherDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

@Service
public class MockWeatherService {

    public WeatherDto forCity(String city, boolean morning) {
        String normalized = city == null ? "Warszawa" : city;
        int hash = Math.abs((normalized.toLowerCase(Locale.ROOT) + LocalDate.now()).hashCode());
        Month month = LocalDate.now().getMonth();
        boolean coldSeason = month == Month.NOVEMBER || month == Month.DECEMBER
                || month == Month.JANUARY || month == Month.FEBRUARY || month == Month.MARCH;

        String[] conditions = coldSeason
                ? new String[]{"rain", "clouds", "clear", "snow", "wind"}
                : new String[]{"clear", "clouds", "rain", "clear", "wind"};
        String condition = conditions[hash % conditions.length];

        int baseTemp = coldSeason ? 3 : 18;
        int temperature = baseTemp + (hash % 7) - (morning ? 2 : 0);

        WeatherDto weather = new WeatherDto();
        weather.setCity(normalized);
        weather.setCondition(condition);
        weather.setTemperatureC(temperature);
        weather.setLabelPl(label(condition, temperature));
        weather.setBikeFriendly(!"rain".equals(condition) && !"snow".equals(condition) && temperature >= 5);
        return weather;
    }

    private String label(String condition, int temperature) {
        String sky = switch (condition) {
            case "rain" -> "deszcz";
            case "snow" -> "śnieg";
            case "clouds" -> "pochmurno";
            case "wind" -> "wietrznie";
            default -> "bezchmurnie";
        };
        return sky + ", " + temperature + "°C";
    }
}

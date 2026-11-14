package com.transitpulse.service;

import com.transitpulse.dto.WeatherDto;
import com.transitpulse.integration.OpenMeteoClient;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    private final CityDirectoryService cityDirectoryService;
    private final OpenMeteoClient openMeteoClient;
    private final MockWeatherService mockWeatherService;

    public WeatherService(
            CityDirectoryService cityDirectoryService,
            OpenMeteoClient openMeteoClient,
            MockWeatherService mockWeatherService
    ) {
        this.cityDirectoryService = cityDirectoryService;
        this.openMeteoClient = openMeteoClient;
        this.mockWeatherService = mockWeatherService;
    }

    public WeatherResult forCity(String city, boolean morning) {
        CityDirectoryService.ResolvedPlace place = cityDirectoryService.resolve(city);
        if (place.hasPoint()) {
            return openMeteoClient.current(place.point())
                    .map(snapshot -> fromOpenMeteo(place.name(), snapshot))
                    .orElseGet(() -> fallback(place.name(), morning));
        }
        return fallback(place.name(), morning);
    }

    private WeatherResult fallback(String city, boolean morning) {
        WeatherDto dto = mockWeatherService.forCity(city, morning);
        return new WeatherResult(dto, "mock");
    }

    private WeatherResult fromOpenMeteo(String city, OpenMeteoClient.OpenMeteoSnapshot snapshot) {
        String condition = mapCondition(snapshot.weatherCode(), snapshot.windSpeedKmh());
        int temperature = (int) Math.round(snapshot.temperatureC());
        WeatherDto dto = new WeatherDto();
        dto.setCity(city);
        dto.setCondition(condition);
        dto.setTemperatureC(temperature);
        dto.setLabelPl(label(condition, temperature));
        dto.setBikeFriendly(isBikeFriendly(condition, temperature, snapshot.windSpeedKmh()));
        return new WeatherResult(dto, "open-meteo");
    }

    private String mapCondition(int code, double windKmh) {
        if (code == 0) {
            return windKmh >= 40 ? "wind" : "clear";
        }
        if (code <= 3) {
            return windKmh >= 45 ? "wind" : "clouds";
        }
        if (code >= 71 && code <= 77) {
            return "snow";
        }
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82) || (code >= 95 && code <= 99)) {
            return "rain";
        }
        return "clouds";
    }

    private boolean isBikeFriendly(String condition, int temperatureC, double windKmh) {
        return !"rain".equals(condition)
                && !"snow".equals(condition)
                && temperatureC >= 5
                && windKmh < 45;
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

    public record WeatherResult(WeatherDto weather, String source) {
    }
}

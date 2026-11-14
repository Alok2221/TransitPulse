package com.transitpulse.controller;

import com.transitpulse.dto.CompareRequest;
import com.transitpulse.dto.CompareResponse;
import com.transitpulse.service.ComparisonService;
import com.transitpulse.service.WeatherService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CompareController {

    private final ComparisonService comparisonService;
    private final WeatherService weatherService;

    public CompareController(ComparisonService comparisonService, WeatherService weatherService) {
        this.comparisonService = comparisonService;
        this.weatherService = weatherService;
    }

    @PostMapping("/compare")
    public CompareResponse compare(@Valid @RequestBody CompareRequest request) {
        WeatherService.WeatherResult weather = weatherService.forCity(request.getDestination(), false);
        return comparisonService.compare(request, weather.weather(), weather.source());
    }
}

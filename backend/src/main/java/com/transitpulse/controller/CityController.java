package com.transitpulse.controller;

import com.transitpulse.dto.CitySuggestionDto;
import com.transitpulse.service.CityDirectoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
public class CityController {

    private final CityDirectoryService cityDirectoryService;

    public CityController(CityDirectoryService cityDirectoryService) {
        this.cityDirectoryService = cityDirectoryService;
    }

    @GetMapping("/search")
    public List<CitySuggestionDto> search(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "6") int limit
    ) {
        return cityDirectoryService.suggest(query, limit);
    }
}

package com.transitpulse.controller;

import com.transitpulse.dto.TripQueryRequest;
import com.transitpulse.dto.TripQueryResponse;
import com.transitpulse.service.TripQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripQueryController {

    private final TripQueryService tripQueryService;

    public TripQueryController(TripQueryService tripQueryService) {
        this.tripQueryService = tripQueryService;
    }

    @GetMapping
    public List<TripQueryResponse> list() {
        return tripQueryService.findAll();
    }

    @GetMapping("/{id}")
    public TripQueryResponse get(@PathVariable Long id) {
        return tripQueryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripQueryResponse create(@Valid @RequestBody TripQueryRequest request) {
        return tripQueryService.create(request);
    }

    @PutMapping("/{id}")
    public TripQueryResponse update(@PathVariable Long id, @Valid @RequestBody TripQueryRequest request) {
        return tripQueryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tripQueryService.delete(id);
    }
}

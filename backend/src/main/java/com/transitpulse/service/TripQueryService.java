package com.transitpulse.service;

import com.transitpulse.domain.TripQuery;
import com.transitpulse.dto.TripQueryRequest;
import com.transitpulse.dto.TripQueryResponse;
import com.transitpulse.repository.TripQueryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class TripQueryService {

    private final TripQueryRepository repository;
    private final CityDistanceService cityDistanceService;

    public TripQueryService(TripQueryRepository repository, CityDistanceService cityDistanceService) {
        this.repository = repository;
        this.cityDistanceService = cityDistanceService;
    }

    public List<TripQueryResponse> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(TripQueryResponse::from)
                .toList();
    }

    public TripQueryResponse findById(Long id) {
        return TripQueryResponse.from(getEntity(id));
    }

    public TripQueryResponse create(TripQueryRequest request) {
        TripQuery entity = new TripQuery();
        apply(entity, request);
        entity.setCreatedAt(Instant.now());
        return TripQueryResponse.from(repository.save(entity));
    }

    public TripQueryResponse update(Long id, TripQueryRequest request) {
        TripQuery entity = getEntity(id);
        apply(entity, request);
        return TripQueryResponse.from(repository.save(entity));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found");
        }
        repository.deleteById(id);
    }

    private void apply(TripQuery entity, TripQueryRequest request) {
        entity.setOrigin(cityDistanceService.normalizeCity(request.getOrigin()));
        entity.setDestination(cityDistanceService.normalizeCity(request.getDestination()));
        entity.setDistanceKm(request.getDistanceKm());
        entity.setPriority(request.getPriority());
        entity.setNotes(request.getNotes());
        entity.setResultSnapshotJson(request.getResultSnapshotJson());
    }

    private TripQuery getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
    }
}

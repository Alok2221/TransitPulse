package com.transitpulse.repository;

import com.transitpulse.domain.TripQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripQueryRepository extends JpaRepository<TripQuery, Long> {
    List<TripQuery> findAllByOrderByCreatedAtDesc();
}

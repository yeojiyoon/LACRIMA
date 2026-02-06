package com.lacrima.lacrimademo.explore.repo;

import com.lacrima.lacrimademo.explore.domain.ExploreEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExploreEventRepository extends JpaRepository<ExploreEvent, Integer> {
    Optional<ExploreEvent> findByCode(String code);
    List<ExploreEvent> findByActiveTrueOrderByNameAsc();
}

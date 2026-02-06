package com.lacrima.lacrimademo.explore.repo;

import com.lacrima.lacrimademo.explore.domain.ExploreMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExploreMapRepository extends JpaRepository<ExploreMap, Long> {
    Optional<ExploreMap> findByCode(String code);
}

package com.lacrima.lacrimademo.explore.repo;

import com.lacrima.lacrimademo.explore.domain.ExplorationState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExplorationStateRepository extends JpaRepository<ExplorationState, Long> {}

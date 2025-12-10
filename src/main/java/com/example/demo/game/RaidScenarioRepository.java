package com.example.demo.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RaidScenarioRepository extends JpaRepository<RaidScenario, Long> {

    // 🔥 방금 우리가 쓰고 있는 메서드
    List<RaidScenario> findByActiveTrue();

    // 🔥 roomId -> 시나리오 찾을 때 쓸 예정인 메서드(있으면 좋고, 지금 당장 필수는 아님)
    Optional<RaidScenario> findById(Long id);
    // 필요하면
    // Optional<RaidScenario> findByName(String name);
}

package com.example.demo.game;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepository extends JpaRepository<Skill, String> {
    // pk가 code(String)이니까 <Skill, String>
}

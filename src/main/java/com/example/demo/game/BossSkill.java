package com.example.demo.game;

import jakarta.persistence.*;

@Entity
@Table(name = "boss_skill")
public class BossSkill {

    // 스킬 이름 = PK
    @Id
    @Column(name = "skill_name", nullable = false, length = 100)
    private String name;

    // 스킬 설명
    @Column(nullable = false, length = 1000)
    private String description;

    // 스킬 효과 설명 (바닐라 텍스트)
    @Column(name = "effect_text", nullable = false, length = 2000)
    private String effectText;

    // 스킬 사용 시 출력할 텍스트
    @Column(name = "use_text", nullable = false, length = 2000)
    private String useText;

    public BossSkill() {
    }

    public BossSkill(String name, String description, String effectText, String useText) {
        this.name = name;
        this.description = description;
        this.effectText = effectText;
        this.useText = useText;
    }

    // getter / setter ...

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEffectText() { return effectText; }
    public void setEffectText(String effectText) { this.effectText = effectText; }

    public String getUseText() { return useText; }
    public void setUseText(String useText) { this.useText = useText; }
}

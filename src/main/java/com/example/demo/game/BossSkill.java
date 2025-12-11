package com.example.demo.game;

import jakarta.persistence.*;

@Entity
@Table(name = "boss_skill")
public class BossSkill {

    // 스킬 식별용 이름 = PK (코드용)
    @Id
    @Column(name = "skill_name", nullable = false, length = 100)
    private String name;

    // 스킬 표시용 이름 (UI에 보여줄 이름)
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    // 스킬 설명
    @Column(nullable = false, length = 1000)
    private String description;

    // 스킬 효과 설명 (바닐라 텍스트)
    @Column(name = "effect_text", nullable = false, length = 2000)
    private String effectText;

    // 스킬 사용 시 출력할 텍스트
    @Column(name = "use_text", nullable = false, length = 2000)
    private String useText;

    // 스킬 최대 쿨타임(턴). 1이면 "쿨 없음" 취급
    @Column(name = "max_cooldown", nullable = false)
    private int maxCooldown;

    public BossSkill() {
    }

    public BossSkill(String name,
                     String displayName,
                     String description,
                     String effectText,
                     String useText,
                     int maxCooldown) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.effectText = effectText;
        this.useText = useText;
        this.maxCooldown = maxCooldown;
    }

    // ==== getter / setter ====

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEffectText() { return effectText; }
    public void setEffectText(String effectText) { this.effectText = effectText; }

    public String getUseText() { return useText; }
    public void setUseText(String useText) { this.useText = useText; }

    public int getMaxCooldown() { return maxCooldown; }
    public void setMaxCooldown(int maxCooldown) { this.maxCooldown = maxCooldown; }
}

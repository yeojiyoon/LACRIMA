package com.example.demo.game;

import jakarta.persistence.*;

@Entity
@Table(name = "skill")
public class Skill {

    // 🔹 pk: 영어 코드 (예: POWER_SLASH)
    @Id
    @Column(length = 50)
    private String code;

    // 🔹 디스플레이용 이름
    @Column(nullable = false, length = 100)
    private String name;

    // 🔹 이미지 경로 (static/img/... or S3 URL 등)
    @Column(length = 255)
    private String imageUrl;

    // 🔹 설명 (툴팁용)
    @Column(length = 1000)
    private String description;

    // 🔹 효과 텍스트 (수치/조건 등)
    @Column(length = 1000)
    private String effectText;

    // 🔹 태그 (ATTACK / DEFENSE / HEAL / BUFF)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillTag tag;

    // 🔹 쿨타임 (턴 수, 사용 안 하면 0)
    private int cooldown;

    // === 기본 생성자 ===
    protected Skill() {}

    // === 편의 생성자/Getter/Setter ===

    public Skill(String code, String name, String imageUrl,
                 String description, String effectText,
                 SkillTag tag, int cooldown) {
        this.code = code;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.effectText = effectText;
        this.tag = tag;
        this.cooldown = cooldown;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEffectText() {
        return effectText;
    }

    public void setEffectText(String effectText) {
        this.effectText = effectText;
    }

    public SkillTag getTag() {
        return tag;
    }

    public void setTag(SkillTag tag) {
        this.tag = tag;
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }
}

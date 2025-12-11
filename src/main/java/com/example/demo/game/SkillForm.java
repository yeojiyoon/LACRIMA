package com.example.demo.game;

public class SkillForm {

    private String code;         // pk
    private String name;         // 디스플레이 이름
    private String imageUrl;     // 이미지 경로
    private String description;  // 설명
    private String effectText;   // 효과 텍스트
    private SkillTag tag;        // 태그
    private Integer cooldown;    // 쿨타임

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

    public Integer getCooldown() {
        return cooldown;
    }

    public void setCooldown(Integer cooldown) {
        this.cooldown = cooldown;
    }
}

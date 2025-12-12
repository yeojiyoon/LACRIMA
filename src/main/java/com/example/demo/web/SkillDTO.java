package com.example.demo.web;

import com.example.demo.game.Skill;

public class SkillDTO {

    private String code;
    private String name;
    private String tag;
    private int cooldown;
    private String imageUrl;

    // ✅ 추가
    private String description;
    private String effectText;

    public SkillDTO(String code, String name, String tag, int cooldown, String imageUrl,
                    String description, String effectText) {
        this.code = code;
        this.name = name;
        this.tag = tag;
        this.cooldown = cooldown;
        this.imageUrl = imageUrl;
        this.description = description;
        this.effectText = effectText;
    }

    public static SkillDTO from(Skill s) {
        if (s == null) return null;
        return new SkillDTO(
                s.getCode(),
                s.getName(),
                s.getTag() != null ? s.getTag().name() : null,
                s.getCooldown(),
                s.getImageUrl(),
                s.getDescription(),   // ✅
                s.getEffectText()     // ✅
        );
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getTag() { return tag; }
    public int getCooldown() { return cooldown; }
    public String getImageUrl() { return imageUrl; }

    // ✅ 추가 getter
    public String getDescription() { return description; }
    public String getEffectText() { return effectText; }
}

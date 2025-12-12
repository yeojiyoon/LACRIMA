package com.example.demo.game;

import java.util.ArrayList;
import java.util.List;

public class MemberListView {

    private Long characterId;
    private String name;

    private int atk;
    private int intStat;  // ✅ 템플릿에서 m.intStat
    private int det;
    private int hpStat;   // ✅ 템플릿에서 m.hpStat (HP 스탯/티어)

    private int hp;       // currentHp
    private int maxHp;
    private int hpRatio;  // 0~100

    private String avatarUrl;

    private List<SkillView> skills = new ArrayList<>();

    public static class SkillView {
        private String code;
        private String displayName;
        private String description;
        private String iconUrl;

        private String tag;     // ✅ 타입
        private int cooldown;   // ✅ 턴(쿨타임)

        public SkillView(String code, String displayName, String description, String iconUrl,
                         String tag, int cooldown) {
            this.code = code;
            this.displayName = displayName;
            this.description = description;
            this.iconUrl = iconUrl;
            this.tag = tag;
            this.cooldown = cooldown;
        }

        public String getCode() { return code; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIconUrl() { return iconUrl; }
        public String getTag() { return tag; }
        public int getCooldown() { return cooldown; }
    }


    // ===== getter/setter =====
    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }

    public int getIntStat() { return intStat; }
    public void setIntStat(int intStat) { this.intStat = intStat; }

    public int getDet() { return det; }
    public void setDet(int det) { this.det = det; }

    public int getHpStat() { return hpStat; }
    public void setHpStat(int hpStat) { this.hpStat = hpStat; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getHpRatio() { return hpRatio; }
    public void setHpRatio(int hpRatio) { this.hpRatio = hpRatio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public List<SkillView> getSkills() { return skills; }
    public void setSkills(List<SkillView> skills) { this.skills = skills; }
}

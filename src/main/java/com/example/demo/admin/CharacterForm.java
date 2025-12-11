package com.example.demo.admin;

import java.util.ArrayList;
import java.util.List;

public class CharacterForm {

    private Long id;
    private Long userId;

    private String name;
    private int atk;
    private int intelligence;
    private int hp;
    private int det;
    private int currentHp;
    private int actionPoint;

    // ===== 새로 추가: UI용 필드 =====
    private String portraitUrl;    // 캐릭터 반신
    private String avatarUrl;      // 캐릭터 두상
    private String catchphrase;    // 캐치프레이즈
    private String oneLiner1;      // 한마디1
    private String oneLiner2;      // 한마디2
    private String oneLiner3;      // 한마디3

    // ===== 새로 추가: 장착 스킬 코드 =====
    private String equippedSkill1Code;
    private String equippedSkill2Code;

    private List<String> inventorySkillCodes = new ArrayList<>();

    // === getter/setter ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }

    public int getIntelligence() { return intelligence; }
    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getDet() { return det; }
    public void setDet(int det) { this.det = det; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public int getActionPoint() { return actionPoint; }
    public void setActionPoint(int actionPoint) { this.actionPoint = actionPoint; }

    public String getPortraitUrl() { return portraitUrl; }
    public void setPortraitUrl(String portraitUrl) { this.portraitUrl = portraitUrl; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCatchphrase() { return catchphrase; }
    public void setCatchphrase(String catchphrase) { this.catchphrase = catchphrase; }

    public String getOneLiner1() { return oneLiner1; }
    public void setOneLiner1(String oneLiner1) { this.oneLiner1 = oneLiner1; }

    public String getOneLiner2() { return oneLiner2; }
    public void setOneLiner2(String oneLiner2) { this.oneLiner2 = oneLiner2; }

    public String getOneLiner3() { return oneLiner3; }
    public void setOneLiner3(String oneLiner3) { this.oneLiner3 = oneLiner3; }

    public String getEquippedSkill1Code() { return equippedSkill1Code; }
    public void setEquippedSkill1Code(String equippedSkill1Code) { this.equippedSkill1Code = equippedSkill1Code; }

    public String getEquippedSkill2Code() { return equippedSkill2Code; }
    public void setEquippedSkill2Code(String equippedSkill2Code) { this.equippedSkill2Code = equippedSkill2Code; }

    public List<String> getInventorySkillCodes() {
        return inventorySkillCodes;
    }

    public void setInventorySkillCodes(List<String> inventorySkillCodes) {
        this.inventorySkillCodes = inventorySkillCodes;
    }
}

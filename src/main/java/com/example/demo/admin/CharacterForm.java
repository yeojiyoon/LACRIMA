package com.example.demo.admin;

public class CharacterForm {

    private Long id;
    private Long userId;      // 소유 유저 ID
    private String name;
    private int atk;
    private int intelligence;
    private int hp;           // HP 스탯(1~5)
    private int det;
    private int currentHp;
    private int actionPoint;

    public CharacterForm() {
    }

    // getter / setter들
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
}

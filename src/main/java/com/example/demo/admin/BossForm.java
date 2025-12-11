package com.example.demo.admin;

public class BossForm {

    private Long id;
    private String name;
    private long maxHp;
    private long currentHp;
    private int defense;

    private int maxActionPoint;
    private int actionPoint;

    // === 추가된 필드: 스킬 3개 이름 ===
    private String skill1Name;
    private String skill2Name;
    private String skill3Name;

    public BossForm() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getMaxHp() { return maxHp; }
    public void setMaxHp(long maxHp) { this.maxHp = maxHp; }

    public long getCurrentHp() { return currentHp; }
    public void setCurrentHp(long currentHp) { this.currentHp = currentHp; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getMaxActionPoint() { return maxActionPoint; }
    public void setMaxActionPoint(int maxActionPoint) { this.maxActionPoint = maxActionPoint; }

    public int getActionPoint() { return actionPoint; }
    public void setActionPoint(int actionPoint) { this.actionPoint = actionPoint; }

    public String getSkill1Name() { return skill1Name; }
    public void setSkill1Name(String skill1Name) { this.skill1Name = skill1Name; }

    public String getSkill2Name() { return skill2Name; }
    public void setSkill2Name(String skill2Name) { this.skill2Name = skill2Name; }

    public String getSkill3Name() { return skill3Name; }
    public void setSkill3Name(String skill3Name) { this.skill3Name = skill3Name; }
}

package com.example.demo.admin;

public class BossForm {

    private Long id;
    private String name;
    private long maxHp;
    private long currentHp;
    private int defense;

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
}

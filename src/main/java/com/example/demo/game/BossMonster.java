package com.example.demo.game;

import jakarta.persistence.*;

@Entity
@Table(name = "boss_monster")
public class BossMonster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 보스 이름 (예: “카오스 드래곤”)
    @Column(nullable = false)
    private String name;

    // 최대 HP
    @Column(nullable = false)
    private long maxHp;

    // 현재 HP
    @Column(nullable = false)
    private long currentHp;

    // 방어력(데미지 계산시 사용)
    @Column(nullable = false)
    private int defense;

    protected BossMonster() {
    }

    public BossMonster(String name, long maxHp, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.defense = defense;
    }

    // getter / setter들
    public Long getId() { return id; }

    public String getName() { return name; }

    public long getMaxHp() { return maxHp; }

    public long getCurrentHp() { return currentHp; }
    public void setCurrentHp(long currentHp) { this.currentHp = currentHp; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public boolean isDead() {
        return currentHp <= 0;
    }
}

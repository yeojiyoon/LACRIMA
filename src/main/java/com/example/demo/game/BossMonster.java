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

    @Column(nullable = false)
    private int maxActionPoint = 1;   // 기본 1, 나중에 관리자 페이지에서 수정 가능

    @Column(nullable = false)
    private int actionPoint = 1;      // 현재 AP

    // ==============================
    // 보스가 가진 스킬 3개 (nullable 허용)
    // ==============================

    @ManyToOne
    @JoinColumn(name = "skill1_name")  // FK -> boss_skill.skill_name
    private BossSkill skill1;

    @ManyToOne
    @JoinColumn(name = "skill2_name")
    private BossSkill skill2;

    @ManyToOne
    @JoinColumn(name = "skill3_name")
    private BossSkill skill3;

    protected BossMonster() {
    }

    public BossMonster(String name, long maxHp, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.defense = defense;
    }

    // === getter / setter들 ===
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

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxHp(long maxHp) {
        this.maxHp = maxHp;
        if (this.currentHp > maxHp) {
            this.currentHp = maxHp;
        }
    }

    // 🔥 AP 관련 getter/setter/유틸
    public int getMaxActionPoint() {
        return maxActionPoint;
    }

    public void setMaxActionPoint(int maxActionPoint) {
        this.maxActionPoint = Math.max(0, maxActionPoint);
        if (this.actionPoint > this.maxActionPoint) {
            this.actionPoint = this.maxActionPoint;
        }
    }

    public int getActionPoint() {
        return actionPoint;
    }

    public void setActionPoint(int actionPoint) {
        this.actionPoint = Math.max(0, actionPoint);
    }

    /** 새 턴 시작 시 호출: 보스 AP 풀로 회복 */
    public void resetActionPoint() {
        this.actionPoint = this.maxActionPoint;
    }

    /** 행동 1회 사용 */
    public void consumeAction() {
        if (this.actionPoint > 0) {
            this.actionPoint--;
        }
    }

    public boolean hasAction() {
        return this.actionPoint > 0;
    }

    public BossSkill getSkill1() {
        return skill1;
    }

    public void setSkill1(BossSkill skill1) {
        this.skill1 = skill1;
    }

    public BossSkill getSkill2() {
        return skill2;
    }

    public void setSkill2(BossSkill skill2) {
        this.skill2 = skill2;
    }

    public BossSkill getSkill3() {
        return skill3;
    }

    public void setSkill3(BossSkill skill3) {
        this.skill3 = skill3;
    }
}

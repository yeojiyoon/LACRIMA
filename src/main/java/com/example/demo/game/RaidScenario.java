package com.example.demo.game;

import jakarta.persistence.*;
@Entity
public class RaidScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "boss_id")
    private BossMonster boss;

    // 🔥 현재 턴 번호 (1턴부터 시작)
    @Column(nullable = false)
    private int currentTurn = 1;

    // 🔥 레이드 활성/비활성
    @Column(nullable = false)
    private boolean active = true;

    public RaidScenario() {}

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BossMonster getBoss() { return boss; }
    public void setBoss(BossMonster boss) { this.boss = boss; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getBossName() {
        return boss != null ? boss.getName() : "(보스 미지정)";
    }

    @Transient
    public String getLobbyRoomId() {
        return "lobby-" + id;
    }

    @Transient
    public String getRaidRoomId() {
        return "raid-" + id;
    }

    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = Math.max(1, currentTurn);
    }
}

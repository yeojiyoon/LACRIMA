package com.example.demo.game;

import jakarta.persistence.*;

@Entity
public class RaidScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 세트 이름 (예: "튜토리얼 레이드")
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    // 🔹 보스 DB와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_id")
    private BossMonster boss;

    public RaidScenario() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BossMonster getBoss() {
        return boss;
    }

    public void setBoss(BossMonster boss) {
        this.boss = boss;
    }

    // 🔹 Thymeleaf에서 편하게 쓰기 위한 이름 헬퍼
    public String getBossName() {
        return boss != null ? boss.getName() : "(보스 미지정)";
    }

    // 🔹 roomId 헬퍼 (그대로 유지)
    @Transient
    public String getLobbyRoomId() {
        return "lobby-" + id;
    }

    @Transient
    public String getRaidRoomId() {
        return "raid-" + id;
    }
}

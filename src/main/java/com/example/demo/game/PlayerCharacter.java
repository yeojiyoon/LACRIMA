package com.example.demo.game;

import com.example.demo.user.UserAccount;
import jakarta.persistence.*;

@Entity
@Table(name = "player_character")
public class PlayerCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 계정과 1:1 연결
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserAccount user;

    @Column(nullable = false)
    private String name;   // 캐릭터 이름 (별칭)

    @Column(nullable = false)
    private int atk;    // 공격력

    @Column(nullable = false)
    private int intelligence;   // 지력

    @Column(nullable = false)
    private int Hp;   // 스탯

    @Column(nullable = false)
    private int det;   // 의지

    @Column(nullable = false)
    private int maxHp;

    @Column(nullable = false)
    private int currentHp; //수치

    @Column (nullable = false)
    private int actionPoint;

    protected PlayerCharacter() {
    }

    public PlayerCharacter(UserAccount user, String name, int atk,
                           int intelligence, int Hp, int det) {
        this.user = user;
        this.name = name;
        this.atk = atk;
        this.intelligence = intelligence;
        this.Hp = Hp;
        this.det = det;
        switch (Hp){
            case 1: maxHp = 120;
                break;
            case 2: maxHp = 140;
                break;
            case 3: maxHp = 160;
                break;
            case 4: maxHp = 180;
                break;
            case 5: maxHp = 200;
                break;
        }
        this.currentHp = maxHp; //DB에서 받아오도록 해야함
        this.actionPoint = 1;
    }

    // getter/setter들
    public Long getId() { return id; }
    public UserAccount getUser() { return user; }

    public String getName() { return name; }
    public int getAtk() { return atk; }
    public int getIntelligence() { return intelligence; }
    public int getHp() { return Hp; }
    public int getDet() { return det; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getActionPoint() { return actionPoint; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public void resetActionPoint() {
        // 죽어있으면 행동포인트도 0 유지
        if (this.currentHp > 0) {
            this.actionPoint = 1;
        } else {
            this.actionPoint = 0;
        }
    }

    public boolean consumeAction() {
        if (actionPoint <= 0) {
            return false;
        }
        this.actionPoint -= 1;
        return true;
    }

    public boolean isDead() {
        return this.currentHp <= 0;
    }

    // 이름 변경
    public void setName(String name) {
        this.name = name;
    }

    // 공격력
    public void setAtk(int atk) {
        this.atk = atk;
    }

    // 지력
    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    // HP 스탯(티어) + maxHp 재계산
    public void setHp(int Hp) {
        this.Hp = Hp;
        switch (Hp) {
            case 1 -> this.maxHp = 120;
            case 2 -> this.maxHp = 140;
            case 3 -> this.maxHp = 160;
            case 4 -> this.maxHp = 180;
            case 5 -> this.maxHp = 200;
            default -> this.maxHp = 120; // 안전장치
        }
        // 현재 HP가 maxHp보다 크면 잘라주기
        if (this.currentHp > this.maxHp) {
            this.currentHp = (int) this.maxHp;
        }
    }

    // 의지
    public void setDet(int det) {
        this.det = det;
    }

    // 행동 포인트 강제 세팅 (관리자용)
    public void setActionPoint(int actionPoint) {
        this.actionPoint = actionPoint;
    }

    // 유저 변경(필요하면)
    public void setUser(UserAccount user) {
        this.user = user;
    }

}

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
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
}

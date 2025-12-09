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
    private int level;

    @Column(nullable = false)
    private int attack;    // 공격력

    @Column(nullable = false)
    private int defense;   // 방어력

    @Column(nullable = false)
    private int maxHp;

    @Column(nullable = false)
    private int currentHp;

    protected PlayerCharacter() {
    }

    public PlayerCharacter(UserAccount user, String name, int level,
                           int attack, int defense, int maxHp) {
        this.user = user;
        this.name = name;
        this.level = level;
        this.attack = attack;
        this.defense = defense;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
    }

    // getter/setter들
    public Long getId() { return id; }
    public UserAccount getUser() { return user; }

    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
}

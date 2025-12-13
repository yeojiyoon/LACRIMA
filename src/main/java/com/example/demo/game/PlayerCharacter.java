package com.example.demo.game;

import com.example.demo.user.UserAccount;
import jakarta.persistence.*;

import java.util.*;

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

    // === 스탯 ===
    @Column(nullable = false)
    private int atk;    // 공격력

    @Column(nullable = false)
    private int intelligence;   // 지력

    @Column(nullable = false)
    private int Hp;   // HP 스탯(티어)

    @Column(nullable = false)
    private int det;   // 의지

    @Column(nullable = false)
    private int maxHp;

    @Column(nullable = false)
    private int currentHp; // 실제 HP 수치

    @Column(nullable = false)
    private int actionPoint;

    // === UI용 정보 ===

    // 반신 이미지 URL
    @Column(length = 255)
    private String portraitUrl;

    // 두상(아이콘) 이미지 URL
    @Column(length = 255)
    private String avatarUrl;

    // 캐치프레이즈 (대표 문구)
    @Column(length = 255)
    private String catchphrase;

    // 캐릭터 한마디 1~3 (간단히 컬럼 3개로)
    @Column(length = 255)
    private String oneLiner1;

    @Column(length = 255)
    private String oneLiner2;

    @Column(length = 255)
    private String oneLiner3;

    // === 스킬 관련 ===

    // 인벤토리 (보유 스킬 목록, N:N)
    @ManyToMany
    @JoinTable(
            name = "character_skill_inventory",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_code")
    )
    private Set<Skill> skillInventory = new HashSet<>();

    // 장착 스킬 1
    @ManyToOne
    @JoinColumn(name = "equipped_skill1_code")
    private Skill equippedSkill1;

    // 장착 스킬 2
    @ManyToOne
    @JoinColumn(name = "equipped_skill2_code")
    private Skill equippedSkill2;

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
        switch (Hp) {
            case 1 -> maxHp = 120;
            case 2 -> maxHp = 140;
            case 3 -> maxHp = 160;
            case 4 -> maxHp = 180;
            case 5 -> maxHp = 200;
            default -> maxHp = 120;
        }
        this.currentHp = maxHp;
        this.actionPoint = 1;
    }

    // ====== getter / setter ======

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

    public String getPortraitUrl() { return portraitUrl; }
    public void setPortraitUrl(String portraitUrl) { this.portraitUrl = portraitUrl; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCatchphrase() { return catchphrase; }
    public void setCatchphrase(String catchphrase) { this.catchphrase = catchphrase; }

    public String getOneLiner1() { return oneLiner1; }
    public void setOneLiner1(String oneLiner1) { this.oneLiner1 = oneLiner1; }

    public String getOneLiner2() { return oneLiner2; }
    public void setOneLiner2(String oneLiner2) { this.oneLiner2 = oneLiner2; }

    public String getOneLiner3() { return oneLiner3; }
    public void setOneLiner3(String oneLiner3) { this.oneLiner3 = oneLiner3; }

    public Set<Skill> getSkillInventory() { return skillInventory; }
    public void setSkillInventory(Set<Skill> skillInventory) { this.skillInventory = skillInventory; }

    public Skill getEquippedSkill1() { return equippedSkill1; }
    public void setEquippedSkill1(Skill equippedSkill1) { this.equippedSkill1 = equippedSkill1; }

    public Skill getEquippedSkill2() { return equippedSkill2; }
    public void setEquippedSkill2(Skill equippedSkill2) { this.equippedSkill2 = equippedSkill2; }

    // ====== 기존 로직들 ======

    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public void resetActionPoint() {
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

    public void setName(String name) { this.name = name; }

    public void setAtk(int atk) { this.atk = atk; }

    public void setIntelligence(int intelligence) { this.intelligence = intelligence; }

    public void setHp(int Hp) {
        this.Hp = Hp;
        switch (Hp) {
            case 1 -> this.maxHp = 120;
            case 2 -> this.maxHp = 140;
            case 3 -> this.maxHp = 160;
            case 4 -> this.maxHp = 180;
            case 5 -> this.maxHp = 200;
            default -> this.maxHp = 120;
        }
        if (this.currentHp > this.maxHp) {
            this.currentHp = this.maxHp;
        }
    }

    @Transient
    public String getRandomOneLiner() {
        List<String> list = new ArrayList<>(3);

        if (oneLiner1 != null && !oneLiner1.isBlank()) list.add(oneLiner1);
        if (oneLiner2 != null && !oneLiner2.isBlank()) list.add(oneLiner2);
        if (oneLiner3 != null && !oneLiner3.isBlank()) list.add(oneLiner3);

        if (list.isEmpty()) return null;

        return list.get(new Random().nextInt(list.size()));
    }

    public void setDet(int det) { this.det = det; }

    public void setActionPoint(int actionPoint) { this.actionPoint = actionPoint; }

    public void setUser(UserAccount user) { this.user = user; }
}

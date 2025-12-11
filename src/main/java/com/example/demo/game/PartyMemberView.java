package com.example.demo.game;

public class PartyMemberView {

    private final Long characterId;
    private final String name;
    private final int hp;
    private final int maxHp;
    private final int hpRatio; // 0~100
    private final int ap;

    // 🔥 스탯 4개
    private final int atkStat;
    private final int intStat;
    private final int detStat;
    private final int hpStat;  // (스탯으로서의 HP, pc.getHp())

    public PartyMemberView(Long characterId,
                           String name,
                           int hp,
                           int maxHp,
                           int ap,
                           int atkStat,
                           int intStat,
                           int detStat,
                           int hpStat) {
        this.characterId = characterId;
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.ap = ap;
        this.atkStat = atkStat;
        this.intStat = intStat;
        this.detStat = detStat;
        this.hpStat = hpStat;

        if (maxHp <= 0) {
            this.hpRatio = 0;
        } else {
            this.hpRatio = (int) Math.round(hp * 100.0 / maxHp);
        }
    }

    public static PartyMemberView from(PlayerCharacter pc) {
        return new PartyMemberView(
                pc.getId(),
                pc.getName(),
                pc.getCurrentHp(),
                pc.getMaxHp(),
                pc.getActionPoint(),
                pc.getAtk(),
                pc.getIntelligence(),
                pc.getDet(),
                pc.getHp()          // 스탯형 HP
        );
    }

    // getter들
    public Long getCharacterId() { return characterId; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getHpRatio() { return hpRatio; }
    public int getAp() { return ap; }

    public int getAtkStat() { return atkStat; }
    public int getIntStat() { return intStat; }
    public int getDetStat() { return detStat; }
    public int getHpStat() { return hpStat; }
}

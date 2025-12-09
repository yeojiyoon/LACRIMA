package com.example.demo.game;

public class PartyMemberView {

    private final Long characterId;   // 🔹 추가
    private final String name;
    private final int hp;
    private final int maxHp;
    private final int hpRatio; // 0~100

    public PartyMemberView(Long characterId, String name, int hp, int maxHp) {
        this.characterId = characterId;
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;

        if (maxHp <= 0) {
            this.hpRatio = 0;
        } else {
            this.hpRatio = (int) Math.round(hp * 100.0 / maxHp);
        }
    }

    public static PartyMemberView from(PlayerCharacter pc) {
        return new PartyMemberView(
                pc.getId(),           // 🔹 캐릭터 PK
                pc.getName(),
                pc.getCurrentHp(),
                pc.getMaxHp()
        );
    }

    public Long getCharacterId() { return characterId; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getHpRatio() { return hpRatio; }
}

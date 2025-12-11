package com.example.demo.game;

public class PartyMemberView {

    private final Long characterId;
    private final String name;
    private final int hp;
    private final int maxHp;
    private final int hpRatio; // 0~100

    // 🔥 현재 남은 AP
    private final int ap;

    public PartyMemberView(Long characterId,
                           String name,
                           int hp,
                           int maxHp,
                           int ap) {
        this.characterId = characterId;
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.ap = ap;

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
                pc.getActionPoint()      // 🔥 여기서 AP 집어넣기
        );
    }

    public Long getCharacterId() { return characterId; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getHpRatio() { return hpRatio; }

    public int getAp() { return ap; }  // 🔥 이게 있어야 Thymeleaf, JSON에서 member.ap 사용 가능
}

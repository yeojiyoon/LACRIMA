// package com.example.demo.game;
package com.example.demo.game;

import org.springframework.stereotype.Service;

//attack 담당
@Service
public class RaidGameService {

    private final BossService bossService;
    private final RaidPartyService raidPartyService;
    private final PlayerCharacterService playerCharacterService;

    public RaidGameService(BossService bossService,
                           RaidPartyService raidPartyService,
                           PlayerCharacterService playerCharacterService) {
        this.bossService = bossService;
        this.raidPartyService = raidPartyService;
        this.playerCharacterService = playerCharacterService;
    }

    /**
     * 레이드 공격 처리 로직
     * - 여기에서 나중에: 스탯, 스킬, 파티 버프, 크리티컬, 로그 기록 등 다 집어넣으면 됨.
     */
    public AttackResult handleAttack(String roomId, String attackerName, PlayerCharacter pc) {

        // (원하면 여기서 attackerName으로 캐릭터/스탯 조회 가능)
        // attackerName으로 조회해서 damage를 계산...
        // PlayerCharacter pc = playerCharacterService.findByUsername(attackerName);
        // int finalDamage = calcDamage(pc, damage, roomId, ...);
        Integer baseDamage = 1; //수정해야 하는 필드
        Integer finalDamage = 10;

        if(pc!=null){
            baseDamage = pc.getAtk();
        }

        finalDamage = calculateDamage(baseDamage);

        String resultText = bossService.attackBoss(roomId, attackerName, finalDamage);
        BossState state = bossService.getBossState(roomId);

        Integer hp = null;
        Integer maxHp = null;
        if (state != null) {
            hp = state.getHp();
            maxHp = state.getMaxHp();
        }

        return new AttackResult(resultText, hp, maxHp, finalDamage);
    }

    // 앞으로:
    // - 스킬 공격 handleSkillAttack(...)
    // - 힐/버프 handleBuff(...)
    // 등으로 확장 가능
    private int calculateDamage(int baseDamage) {
        int diceCount;
        int diceSides;

        switch (baseDamage) {
            case 1:
                diceCount = 1;
                diceSides = 2;
                break;
            case 2:
                diceCount = 1;
                diceSides = 4;
                break;
            case 3:
                diceCount = 2;
                diceSides = 6;
                break;
            case 4:
                diceCount = 3;
                diceSides = 6;
                break;
            case 5:
                diceCount = 4;
                diceSides = 6;
                break;
            default:
                // 범위를 벗어나면 가장 약한 조합으로
                diceCount = 1;
                diceSides = 2;
        }

        // 각 조합은 "diceCount번 굴린 주사위를 두 번 합산"
        return rollDice(diceCount, diceSides) + rollDice(diceCount, diceSides);
    }

    private int rollDice(int count, int sides) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            total += (int) (Math.random() * sides) + 1; // 1 ~ sides
        }
        return total;
    }
}

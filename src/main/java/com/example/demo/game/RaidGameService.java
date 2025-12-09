package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// attack 담당
@Service
public class RaidGameService {

    private final BossService bossService;
    private final RaidPartyService raidPartyService;
    private final PlayerCharacterService playerCharacterService;

    // roomId -> 현재 턴 번호
    private final Map<String, Integer> roomTurns = new ConcurrentHashMap<>();

    public RaidGameService(BossService bossService,
                           RaidPartyService raidPartyService,
                           PlayerCharacterService playerCharacterService) {
        this.bossService = bossService;
        this.raidPartyService = raidPartyService;
        this.playerCharacterService = playerCharacterService;
    }

    // 레이드 방별 턴 계산
    private int getCurrentTurn(String roomId) {
        return roomTurns.computeIfAbsent(roomId, id -> 1); // 기본 1턴부터 시작
    }

    private int nextTurn(String roomId) {
        return roomTurns.merge(roomId, 1, Integer::sum);
    }

    /**
     * 레이드 공격 처리 로직
     */
    public AttackResult handleAttack(String roomId, String attackerName, PlayerCharacter pc) {

        int baseDamage;
        int finalDamage;

        int currentTurn = getCurrentTurn(roomId);

        // 1) 캐릭터가 없는 경우(비로그인 등)는 일단 허용(기본 공격)
        if (pc == null) {
            baseDamage = 1;
            finalDamage = calculateDamage(baseDamage);

            String resultText = bossService.attackBoss(roomId, attackerName, finalDamage);
            BossState state = bossService.getBossState(roomId);

            int hp = (state != null) ? state.getHp() : 0;
            int maxHp = (state != null) ? state.getMaxHp() : 0;

            return new AttackResult(
                    resultText,
                    finalDamage,
                    hp,
                    maxHp,
                    currentTurn,
                    false
            );
        } else {
            baseDamage = pc.getAtk();
        }

        // 2) 이미 행동한 경우
        if (pc.getActionPoint() <= 0) {
            BossState state = bossService.getBossState(roomId);
            int hp = (state != null) ? state.getHp() : 0;
            int maxHp = (state != null) ? state.getMaxHp() : 0;

            String msg = pc.getName() + "는 이미 이번 턴에 행동했습니다.";
            return new AttackResult(
                    msg,
                    0,
                    hp,
                    maxHp,
                    currentTurn,
                    false
            );
        }

        // 3) 행동 1 소모 + 저장
        pc.consumeAction();
        playerCharacterService.save(pc);

        // 4) 실제 데미지 계산
        finalDamage = calculateDamage(baseDamage);

        String resultText = bossService.attackBoss(roomId, attackerName, finalDamage);
        BossState state = bossService.getBossState(roomId);

        int hp = (state != null) ? state.getHp() : 0;
        int maxHp = (state != null) ? state.getMaxHp() : 0;

        // 5) 파티 전원이 행동을 다 썼는지 체크
        boolean allDone = areAllActionsConsumed(roomId);

        if (!allDone) {
            // 아직 턴 안 끝남
            return new AttackResult(
                    resultText,
                    finalDamage,
                    hp,
                    maxHp,
                    currentTurn,
                    false
            );
        }

        // 🔥 6) 여기서 "플레이어 턴 종료 → 보스 턴 → 다음 턴 시작"

        // 6-1) 보스 턴 로직
        String bossTurnMessage = performBossTurn(roomId);

        // 6-2) 모든 캐릭터 actionPoint를 1로 리셋
        resetPartyActions(roomId);

        // 6-3) 턴 증가
        int nextTurnNumber = nextTurn(roomId);

        String fullMessage = resultText + " / " + bossTurnMessage +
                " / " + nextTurnNumber + "턴 시작";

        return new AttackResult(
                fullMessage,
                finalDamage,
                hp,
                maxHp,
                nextTurnNumber,
                true
        );
    }

    // --- 데미지 계산 (주사위) ---

    private int calculateDamage(int baseDamage) {
        int diceCount;
        int diceSides;

        switch (baseDamage) {
            case 1:
                diceCount = 1; diceSides = 2; break;
            case 2:
                diceCount = 1; diceSides = 4; break;
            case 3:
                diceCount = 2; diceSides = 6; break;
            case 4:
                diceCount = 3; diceSides = 6; break;
            case 5:
                diceCount = 4; diceSides = 6; break;
            default:
                diceCount = 1; diceSides = 2; // 범위를 벗어나면 가장 약한 조합
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

    // --- 파티 전원 행동 소진 체크 ---

    private boolean areAllActionsConsumed(String roomId) {
        var partyMembers = raidPartyService.getPartyMembers(roomId);
        if (partyMembers == null || partyMembers.isEmpty()) {
            return false;
        }

        for (PartyMemberView view : partyMembers) {
            Long characterId = view.getCharacterId();
            PlayerCharacter pc = playerCharacterService.findById(characterId);
            if (pc != null && pc.getActionPoint() > 0) {
                return false; // 아직 행동 남은 사람 있음
            }
        }
        return true;
    }

    // --- 보스 턴 ---

    // RaidGameService 내부

    private String performBossTurn(String roomId) {
        var partyMembers = raidPartyService.getPartyMembers(roomId);
        if (partyMembers == null || partyMembers.isEmpty()) {
            return "보스가 공격했으나 대상이 없습니다.";
        }

        int damagePerPlayer = 10; // 임시 고정값. 나중에 보스 스킬/랜덤으로 바꿔도 됨.

        for (PartyMemberView view : partyMembers) {
            Long characterId = view.getCharacterId(); // 👉 PartyMemberView에 characterId 추가돼 있어야 함
            PlayerCharacter pc = playerCharacterService.findById(characterId);
            if (pc == null) continue;

            int newHp = Math.max(0, pc.getCurrentHp() - damagePerPlayer);
            pc.setCurrentHp(newHp);
            playerCharacterService.save(pc);  // 🔥 DB에 반영
        }

        return "보스가 파티 전원에게 " + damagePerPlayer + " 피해를 입혔습니다.";
    }


    // --- 파티 행동포인트 리셋 ---

    private void resetPartyActions(String roomId) {
        var partyMembers = raidPartyService.getPartyMembers(roomId);
        if (partyMembers == null) return;

        for (PartyMemberView view : partyMembers) {
            Long characterId = view.getCharacterId();
            PlayerCharacter pc = playerCharacterService.findById(characterId);
            if (pc == null) continue;

            pc.resetActionPoint();
            playerCharacterService.save(pc);
        }
    }
}

package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 전투 로직
@Service
public class RaidGameService {

    private final BossService bossService;
    private final RaidPartyService raidPartyService;
    private final PlayerCharacterService playerCharacterService;

    // roomId -> 현재 턴 번호
    private final Map<String, Integer> roomTurns = new ConcurrentHashMap<>();

    // roomId -> (targetCharacterId -> defenderCharacterId)
    private final Map<String, Map<Long, Long>> roomGuards = new ConcurrentHashMap<>();

    private Map<Long, Long> getGuardMap(String roomId) {
        return roomGuards.computeIfAbsent(roomId, id -> new ConcurrentHashMap<>());
    }

    private void clearGuards(String roomId) {
        Map<Long, Long> guards = roomGuards.get(roomId);
        if (guards != null) guards.clear();
    }

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

        return ifEnded(roomId, resultText, 1, finalDamage, hp, maxHp);
    }

    public AttackResult handleDefend(String roomId,
                                     PlayerCharacter defender,
                                     Long targetCharId,
                                     String comment) {

        int currentTurn = getCurrentTurn(roomId);

        // 0) 보스 상태 가져오기 (메시지용 / HP 표시용)
        BossState state = bossService.getBossState(roomId);
        int hp = (state != null) ? state.getHp() : 0;
        int maxHp = (state != null) ? state.getMaxHp() : 0;

        // 1) 행동 포인트 체크
        if (defender.getActionPoint() <= 0) {
            String msg = defender.getName() + "는 이미 이번 턴에 행동했습니다.";
            return new AttackResult(
                    msg,
                    0,      // damage 없음
                    hp,
                    maxHp,
                    currentTurn,
                    false   // 턴 안 끝남
            );
        }

        // 2) 파티에 실제로 그 대상이 존재하는지 확인 (선택)
        var partyMembers = raidPartyService.getPartyMembers(roomId);
        boolean targetExists = partyMembers.stream()
                .anyMatch(p -> p.getCharacterId().equals(targetCharId));
        if (!targetExists) {
            String msg = "해당 방어 대상이 파티에 없습니다.";
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
        defender.consumeAction();
        playerCharacterService.save(defender);

        // 4) 방어 관계 기록 (roomId -> target -> defender)
        Map<Long, Long> guards = getGuardMap(roomId);
        guards.put(targetCharId, defender.getId());

        // 방어 텍스트 (comment 있으면 붙이기)
        String baseMessage =
                defender.getName() + "는 " + targetCharId + "을(를) 방어 자세로 보호합니다.";
        if (comment != null && !comment.isBlank()) {
            baseMessage += " (" + comment + ")";
        }

        // 5) 모두 행동했는지 체크
        return ifEnded(roomId, baseMessage, 2, 0, hp, maxHp);
    }

    public AttackResult ifEnded(String roomId,
                                String resultText,
                                int mode,
                                int finalDamage,
                                int hp,
                                int maxHp) {
        boolean allDone = areAllActionsConsumed(roomId);
        int currentTurn = getCurrentTurn(roomId);  // 🔥 이 턴이 지금 플레이 중인 턴

        if (!allDone) {
            // 아직 턴 안 끝났으면, 그냥 현재 턴 번호로 반환
            return new AttackResult(
                    resultText,
                    finalDamage,
                    hp,
                    maxHp,
                    currentTurn,
                    false,
                    null
            );
        } else {
            // 🔥 보스 턴 실행해서 각 타격 정보 받아오기
            java.util.List<BossHit> bossHits = performBossTurn(roomId);

            // 보스 HP 최신값
            BossState state = bossService.getBossState(roomId);
            int newHp = (state != null) ? state.getHp() : hp;
            int newMaxHp = (state != null) ? state.getMaxHp() : maxHp;

            // 행동 포인트 리셋 + 턴 증가
            resetPartyActions(roomId);
            int nextTurnNumber = nextTurn(roomId);  // 🔥 이 값은 "다음 턴 번호"지만,
            // AttackResult에는 굳이 넣지 않는다. (TURN_START에서 별도로 쓸 것)

            // AttackResult.message 는 "플레이어 행동 로그"만 유지
            return new AttackResult(
                    resultText,
                    finalDamage,
                    newHp,
                    newMaxHp,
                    currentTurn,  // 🔥 여전히 "이번 턴 번호"
                    true,
                    bossHits
            );
        }
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

    private int calculateDefense(PlayerCharacter defender) {
        int det = defender.getDet(); // ⬅ 의지 수치(1~5)

        int diceCount;
        int diceSides;
        int bonus;

        switch (det) {
            case 1 -> { diceCount = 1; diceSides = 4; bonus = 4; }
            case 2 -> { diceCount = 1; diceSides = 4; bonus = 5; }
            case 3 -> { diceCount = 1; diceSides = 6; bonus = 8; }
            case 4 -> { diceCount = 1; diceSides = 10; bonus = 10; }
            case 5 -> { diceCount = 2; diceSides = 8; bonus = 12; }
            default -> { diceCount = 1; diceSides = 4; bonus = 0; }
        }

        int rolled = rollDice(diceCount, diceSides); // 이미 있는 함수
        return rolled + bonus;
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
    private java.util.List<BossHit> performBossTurn(String roomId) {
        var partyMembers = raidPartyService.getPartyMembers(roomId);
        if (partyMembers == null || partyMembers.isEmpty()) {
            return java.util.List.of(); // 대상 없음
        }

        int damagePerPlayer = 10; // 나중에 보스 스킬 데미지/랜덤으로 교체 예정
        Map<Long, Long> guards = getGuardMap(roomId);

        java.util.List<BossHit> hits = new java.util.ArrayList<>();

        for (PartyMemberView view : partyMembers) {
            Long targetId = view.getCharacterId();
            PlayerCharacter target = playerCharacterService.findById(targetId);
            if (target == null) continue;

            int incomingDamage = damagePerPlayer;
            Integer defenseUsed = null;  // 🔥 이번 타격에서 사용된 방어값 (없으면 null)

            // 이 타겟을 방어하는 캐릭터가 있는지 확인
            Long defenderId = guards.get(targetId);
            if (defenderId != null) {
                PlayerCharacter defender = playerCharacterService.findById(defenderId);
                if (defender != null) {
                    int defense = calculateDefense(defender);
                    defenseUsed = defense;  // 🔥 기록
                    incomingDamage = Math.max(0, damagePerPlayer - defense);
                }
            }

            int oldHp = target.getCurrentHp();
            int newHp = Math.max(0, oldHp - incomingDamage);
            target.setCurrentHp(newHp);
            playerCharacterService.save(target);

            // 🔥 이번 타격 정보 기록 (defense 함께)
            hits.add(new BossHit(
                    target.getId(),
                    target.getName(),
                    incomingDamage,      // 실제 들어간 피해량
                    newHp,               // 맞고 난 뒤 HP
                    target.getMaxHp(),
                    defenseUsed          // 🔥 여기
            ));
        }

        // 턴 끝났으니 방어 상태 초기화
        clearGuards(roomId);

        return hits;
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

    public static class BossHit { //boss 결과 DTO
        private final Long characterId;
        private final String name;
        private final int damage;
        private final int hpAfter;
        private final int maxHp;
        private final Integer defense;   // 🔥 추가: 사용된 방어값 (없으면 null)

        public BossHit(Long characterId,
                       String name,
                       int damage,
                       int hpAfter,
                       int maxHp,
                       Integer defense) {   // 🔥 생성자에도 추가
            this.characterId = characterId;
            this.name = name;
            this.damage = damage;
            this.hpAfter = hpAfter;
            this.maxHp = maxHp;
            this.defense = defense;
        }


        public Long getCharacterId() { return characterId; }
        public String getName() { return name; }
        public int getDamage() { return damage; }
        public int getHpAfter() { return hpAfter; }
        public int getMaxHp() { return maxHp; }
        public Integer getDefense() { return defense; }  // 🔥 getter
    }

    //레이드 시작시 action
    public void startBattle(String roomId) {
        if (roomId == null || roomId.isBlank()) return;

        // 1) 턴을 1로 설정
        roomTurns.put(roomId, 1);

        // 2) 방어 관계 초기화
        clearGuards(roomId);

        // 3) 파티 모든 행동 포인트 회복
        resetPartyActions(roomId);

        // 4) 필요하면 보스 초기화도 여기서
        // bossService.initBoss(roomId);
    }

    // RaidGameService 내부에 추가
    public int getTurn(String roomId) {
        return roomTurns.getOrDefault(roomId, 0);
    }
}
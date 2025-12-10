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
    private final RaidScenarioRepository raidScenarioRepository;  // 🔥 추가

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
                           PlayerCharacterService playerCharacterService,
                           RaidScenarioRepository raidScenarioRepository) {   // 🔥 추가
        this.bossService = bossService;
        this.raidPartyService = raidPartyService;
        this.playerCharacterService = playerCharacterService;
        this.raidScenarioRepository = raidScenarioRepository;
    }

    // 레이드 방별 턴 계산
    private int getCurrentTurn(String roomId) {
        return roomTurns.computeIfAbsent(roomId, id -> 1); // 기본 1턴부터 시작
    }

    private int nextTurn(String roomId) {
        return roomTurns.merge(roomId, 1, Integer::sum);
    }

    // 🔥 외부에서 읽는 용도
    public int getTurn(String roomId) {
        return roomTurns.getOrDefault(roomId, 0);
    }

    // 🔥 레이드 시작시 action
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

    // 🔥 시나리오 비활성화 (보스 승리/패배 후)
    // roomId 형식이 "raid-{id}" 라는 가정하에 id를 파싱해서 비활성화
    private void deactivateScenario(String roomId) {
        if (roomId == null || !roomId.startsWith("raid-")) return;

        try {
            Long scenarioId = Long.parseLong(roomId.substring("raid-".length()));
            raidScenarioRepository.findById(scenarioId)
                    .ifPresent(s -> {
                        s.setActive(false);       // 🔥 RaidScenario에 active 필드 & setter 반드시 추가
                        raidScenarioRepository.save(s);
                    });
        } catch (NumberFormatException e) {
            // roomId 파싱 실패하면 그냥 무시
        }
    }

    // 🔥 파티 전원 사망 체크
    private boolean isPartyAllDead(String roomId) {
        var partyMembers = raidPartyService.getPartyMembers(roomId);
        if (partyMembers == null || partyMembers.isEmpty()) return false;

        for (PartyMemberView view : partyMembers) {
            PlayerCharacter pc = playerCharacterService.findById(view.getCharacterId());
            if (pc != null && pc.getCurrentHp() > 0) {
                return false;
            }
        }
        return true;
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

        // 🔥 보스 사망 체크
        if (hp <= 0) {
            // 레이드 비활성화
            deactivateScenario(roomId);

            // 보스 턴, 다음 턴 없음
            return new AttackResult(
                    resultText,
                    finalDamage,
                    hp,
                    maxHp,
                    currentTurn,
                    true,      // 턴은 사실상 끝
                    null,      // bossHits 없음
                    true,      // bossDead
                    false      // partyWiped
            );
        }

        // 🔥 보스 살아 있으면 평소처럼 턴 종료 여부 체크
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
        int currentTurn = getCurrentTurn(roomId);  // 지금 플레이 중인 턴

        if (!allDone) {
            return new AttackResult(
                    resultText,
                    finalDamage,
                    hp,
                    maxHp,
                    currentTurn,
                    false,
                    null,
                    false,
                    false
            );
        } else {
            // 🔥 보스 턴 실행
            java.util.List<BossHit> bossHits = performBossTurn(roomId);

            BossState state = bossService.getBossState(roomId);
            int newHp = (state != null) ? state.getHp() : hp;
            int newMaxHp = (state != null) ? state.getMaxHp() : maxHp;

            // 🔥 파티 전원 사망 체크
            boolean partyWiped = isPartyAllDead(roomId);
            if (partyWiped) {
                deactivateScenario(roomId);
            }

            // 행동 포인트 리셋 + 턴 증가 (전멸이어도 숫자만 올라감)
            resetPartyActions(roomId);
            int nextTurnNumber = nextTurn(roomId);

            return new AttackResult(
                    resultText,
                    finalDamage,
                    newHp,
                    newMaxHp,
                    currentTurn,  // 이번 턴 번호
                    true,
                    bossHits,
                    false,         // bossDead
                    partyWiped     // partyWiped
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

        return rollDice(diceCount, diceSides) + rollDice(diceCount, diceSides);
    }

    private int calculateDefense(PlayerCharacter defender) {
        int det = defender.getDet(); // 의지 수치(1~5)

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

        int rolled = rollDice(diceCount, diceSides);
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

        int damagePerPlayer = 10; // TODO: 보스 스킬 데미지/랜덤으로 교체 예정
        Map<Long, Long> guards = getGuardMap(roomId);

        java.util.List<BossHit> hits = new java.util.ArrayList<>();

        for (PartyMemberView view : partyMembers) {
            Long targetId = view.getCharacterId();
            PlayerCharacter target = playerCharacterService.findById(targetId);
            if (target == null) continue;

            int incomingDamage = damagePerPlayer;
            Integer defenseUsed = null;

            Long defenderId = guards.get(targetId);
            if (defenderId != null) {
                PlayerCharacter defender = playerCharacterService.findById(defenderId);
                if (defender != null) {
                    int defense = calculateDefense(defender);
                    defenseUsed = defense;
                    incomingDamage = Math.max(0, damagePerPlayer - defense);
                }
            }

            int oldHp = target.getCurrentHp();
            int newHp = Math.max(0, oldHp - incomingDamage);
            target.setCurrentHp(newHp);
            playerCharacterService.save(target);

            hits.add(new BossHit(
                    target.getId(),
                    target.getName(),
                    incomingDamage,
                    newHp,
                    target.getMaxHp(),
                    defenseUsed
            ));
        }

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

    public static class BossHit { // boss 결과 DTO
        private final Long characterId;
        private final String name;
        private final int damage;
        private final int hpAfter;
        private final int maxHp;
        private final Integer defense;

        public BossHit(Long characterId,
                       String name,
                       int damage,
                       int hpAfter,
                       int maxHp,
                       Integer defense) {
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
        public Integer getDefense() { return defense; }
    }
}

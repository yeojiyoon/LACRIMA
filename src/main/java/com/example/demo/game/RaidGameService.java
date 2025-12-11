package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

// 전투 로직
@Service
public class RaidGameService {

    private final BossService bossService;
    private final RaidPartyService raidPartyService;
    private final PlayerCharacterService playerCharacterService;
    private final RaidScenarioRepository raidScenarioRepository;  // 시나리오
    private final BossSkillExecutor bossSkillExecutor;           // ★ 보스 스킬 실행기

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
                           RaidScenarioRepository raidScenarioRepository,
                           BossSkillExecutor bossSkillExecutor) {
        this.bossService = bossService;
        this.raidPartyService = raidPartyService;
        this.playerCharacterService = playerCharacterService;
        this.raidScenarioRepository = raidScenarioRepository;
        this.bossSkillExecutor = bossSkillExecutor;
    }

    // ================== 턴 / 시나리오 헬퍼 ==================

    // roomId("raid-1") -> RaidScenario
    private RaidScenario findScenario(String roomId) {
        if (roomId == null || !roomId.startsWith("raid-")) return null;

        try {
            Long scenarioId = Long.parseLong(roomId.substring("raid-".length()));
            return raidScenarioRepository.findById(scenarioId).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 🔥 현재 턴 조회 (없으면 1로 초기화)
    private int getCurrentTurn(String roomId) {
        RaidScenario sc = findScenario(roomId);
        if (sc == null) {
            return 1;
        }
        int turn = sc.getCurrentTurn();
        if (turn <= 0) {
            sc.setCurrentTurn(1);
            raidScenarioRepository.save(sc);
            return 1;
        }
        return turn;
    }

    // 🔥 다음 턴으로 +1 하고 DB 저장
    private int nextTurn(String roomId) {
        RaidScenario sc = findScenario(roomId);
        if (sc == null) {
            return 1;
        }
        int next = getCurrentTurn(roomId) + 1;
        sc.setCurrentTurn(next);
        raidScenarioRepository.save(sc);
        return next;
    }

    // 🔥 외부(핸들러/클라)에서 읽는 용도
    public int getTurn(String roomId) {
        return getCurrentTurn(roomId);
    }

    // 🔥 레이드 시작시
    public void startBattle(String roomId) {
        if (roomId == null || roomId.isBlank()) return;

        // 1) 해당 레이드 시나리오 턴 1로 리셋
        RaidScenario sc = findScenario(roomId);
        if (sc != null) {
            sc.setCurrentTurn(1);
            raidScenarioRepository.save(sc);
        }

        // 2) 방어 관계 초기화
        clearGuards(roomId);

        // 3) 파티 모든 행동 포인트 회복
        resetPartyActions(roomId);

        // 4) 보스 초기화는 기존처럼 Admin API 등에서 따로 처리
        // bossService.initBoss(roomId);
    }

    // 🔥 시나리오 비활성화 (보스 승리/패배 후)
    private void deactivateScenario(String roomId) {
        RaidScenario sc = findScenario(roomId);
        if (sc == null) return;

        sc.setActive(false);
        raidScenarioRepository.save(sc);
    }

    // 🔥 roomId -> BossMonster 찾기 (RaidScenario 관계 이용)
    private BossMonster findBossMonster(String roomId) {
        RaidScenario sc = findScenario(roomId);
        if (sc == null) return null;
        return sc.getBoss();
    }

    // 🔥 이번 턴에 사용할 보스 스킬 선택 (쿨타임 + 우선순위 적용)
    private BossSkill chooseBossSkillForTurn(String roomId, BossMonster boss) {
        return bossSkillExecutor.chooseSkillForTurn(roomId, boss);
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

    // ================== 공격 / 방어 ==================

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

        // 2) 파티에 실제로 그 대상이 존재하는지 확인
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

        // 🔸 아직 누군가 행동이 남아 있으면 그냥 반환
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
                    false,
                    null
            );
        }

        // 🔥 여기부터 "라운드 종료 → 보스 턴 → 다음 턴" 처리

        // 1) 보스 턴 실행 (스킬 정보 포함)
        BossTurnResult bossTurn = performBossTurn(roomId);
        List<BossHit> bossHits = bossTurn.getHits();
        String skillText = bossTurn.getSkillText(); // "[보스] 가 XXX을 시전한다..."

        BossState state = bossService.getBossState(roomId);
        int newHp = (state != null) ? state.getHp() : hp;
        int newMaxHp = (state != null) ? state.getMaxHp() : maxHp;

        // 2) 파티 전원 사망 체크
        boolean partyWiped = isPartyAllDead(roomId);
        if (partyWiped) {
            deactivateScenario(roomId);
        }

        // 3) 행동 포인트 리셋 + 턴 증가
        resetPartyActions(roomId);
        int nextTurnNumber = nextTurn(roomId); // ChatHandler에서 getTurn()으로 읽음

        // 4) 이번 턴에 대한 AttackResult 반환
        return new AttackResult(
                resultText,       // 플레이어 공격/방어 로그
                finalDamage,
                newHp,
                newMaxHp,
                currentTurn,      // 이번 턴 번호
                true,             // 턴 종료
                bossHits,
                false,            // bossDead (여기선 아직 아님)
                partyWiped,
                skillText         // 보스 스킬 로그
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
            // 🔥 HP 0(사망)은 무시, 살아 있으면서 AP > 0이면 아직 턴 끝 아님
            if (pc != null && pc.getCurrentHp() > 0 && pc.getActionPoint() > 0) {
                return false;
            }
        }
        return true;
    }

    // --- 보스 턴 (보스 스킬 반영) ---
    private BossTurnResult performBossTurn(String roomId) {
        var partyMembers = raidPartyService.getPartyMembers(roomId);
        if (partyMembers == null || partyMembers.isEmpty()) {
            return new BossTurnResult(List.of(), null);
        }

        int currentTurn = getCurrentTurn(roomId);

        // 🔥 살아있는 멤버만 필터링
        List<PartyMemberView> aliveMembers = new ArrayList<>();
        for (PartyMemberView view : partyMembers) {
            PlayerCharacter pc = playerCharacterService.findById(view.getCharacterId());
            if (pc != null && pc.getCurrentHp() > 0) {
                aliveMembers.add(view);
            }
        }

        BossMonster bossMonster = findBossMonster(roomId);
        BossSkill skillToUse = chooseBossSkillForTurn(roomId, bossMonster);

        Map<Long, Integer> attackPlan = new HashMap<>();
        if (skillToUse != null) {
            // ★ 살아있는 멤버 기준으로만 계획
            attackPlan = bossSkillExecutor.createAttackPlan(
                    skillToUse,
                    aliveMembers,
                    currentTurn
            );
        } else {
            // 보스/스킬 없으면 기본값: 살아있는 애들 전원에게 10 데미지
            for (PartyMemberView view : aliveMembers) {
                attackPlan.put(view.getCharacterId(), 10);
            }
        }

        Map<Long, Long> guards = getGuardMap(roomId);
        List<BossHit> hits = new ArrayList<>();

        for (PartyMemberView view : partyMembers) { // 죽은 애 포함돼 있어도 상관 없음
            Long targetId = view.getCharacterId();
            Integer plannedDamage = attackPlan.get(targetId);
            if (plannedDamage == null || plannedDamage <= 0) {
                continue; // 이 스킬에서는 공격 대상이 아님
            }

            PlayerCharacter target = playerCharacterService.findById(targetId);
            if (target == null) continue;

            int incomingDamage = plannedDamage;
            Integer defenseUsed = null;

            Long defenderId = guards.get(targetId);
            if (defenderId != null) {
                PlayerCharacter defender = playerCharacterService.findById(defenderId);
                if (defender != null) {
                    int defense = calculateDefense(defender);
                    defenseUsed = defense;
                    incomingDamage = Math.max(0, plannedDamage - defense);
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

        // 스킬 사용 텍스트 구성
        String skillText = null;
        if (skillToUse != null) {
            String name = skillToUse.getName();
            String desc = skillToUse.getDescription();
            skillText = "[보스] 가 " + name + "을 시전한다.\n";
            if (desc != null && !desc.isBlank()) {
                skillText += " - " + desc;
            }
        }

        return new BossTurnResult(hits, skillText);
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

    // ================== DTO들 ==================

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

    // --- 보스 턴 결과 DTO (스킬 정보 + 각 타격 결과) ---
    public static class BossTurnResult {
        private final List<BossHit> hits;
        private final String skillText; // "[보스] 가 XXX을 시전한다..." 같은 문자열

        public BossTurnResult(List<BossHit> hits, String skillText) {
            this.hits = hits;
            this.skillText = skillText;
        }

        public List<BossHit> getHits() {
            return hits;
        }

        public String getSkillText() {
            return skillText;
        }
    }

    // 🔥 UI용 쿨타임 정보 조회 (실제 로직은 BossSkillExecutor가 다 함)
    public BossSkillExecutor.SkillCooldownInfo getSkillCooldownInfo(String roomId) {
        BossMonster boss = findBossMonster(roomId); // 이미 있는 private 메서드
        return bossSkillExecutor.getSkillCooldownInfo(roomId, boss);
    }

}

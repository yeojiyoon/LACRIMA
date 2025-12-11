package com.example.demo.game;

import java.util.List;

public class AttackResult {

    private final String message;   // 로그 텍스트
    private final int damage;       // 이번 공격으로 입힌 데미지
    private final int bossHp;       // 보스 현재 HP
    private final int maxHp;        // 보스 최대 HP

    private final int turn;         // 현재 턴 번호
    private final boolean turnEnded; // 이 공격(또는 방어)으로 플레이어 턴이 끝났는지

    // 🔥 보스가 각 파티원에게 가한 타격 정보 (보스 턴이 아닐 땐 null)
    private final List<RaidGameService.BossHit> bossHits;

    // 🔥 엔딩 판정용
    private final boolean bossDead;    // 이 행동으로 보스가 죽었는지
    private final boolean partyWiped;  // 보스 턴 후 파티 전원 사망인지

    // 🔥 보스 스킬 로그용 (예: "[보스 스킬] 브레스 - 공격을 한다.")
    private final String bossSkillText;

    // ✅ 예전처럼 쓰던 6개짜리 생성자 (bossHits = null, 엔딩 플래그 false, 스킬로그 없음)
    public AttackResult(String message,
                        int damage,
                        int bossHp,
                        int maxHp,
                        int turn,
                        boolean turnEnded) {
        this(message, damage, bossHp, maxHp, turn, turnEnded,
                null, false, false, null);
    }

    // ✅ 기존 7개짜리 생성자 (bossHits만 있는 버전)
    public AttackResult(String message,
                        int damage,
                        int bossHp,
                        int maxHp,
                        int turn,
                        boolean turnEnded,
                        List<RaidGameService.BossHit> bossHits) {
        this(message, damage, bossHp, maxHp, turn, turnEnded,
                bossHits, false, false, null);
    }

    // ✅ 기존 9개짜리 생성자 → 내부적으로 10개짜리로 위임
    public AttackResult(String message,
                        int damage,
                        int bossHp,
                        int maxHp,
                        int turn,
                        boolean turnEnded,
                        List<RaidGameService.BossHit> bossHits,
                        boolean bossDead,
                        boolean partyWiped) {
        this(message, damage, bossHp, maxHp, turn, turnEnded,
                bossHits, bossDead, partyWiped, null);
    }

    // ✅ 새로 추가된 10개짜리 생성자 (보스 스킬 텍스트까지)
    public AttackResult(String message,
                        int damage,
                        int bossHp,
                        int maxHp,
                        int turn,
                        boolean turnEnded,
                        List<RaidGameService.BossHit> bossHits,
                        boolean bossDead,
                        boolean partyWiped,
                        String bossSkillText) {
        this.message = message;
        this.damage = damage;
        this.bossHp = bossHp;
        this.maxHp = maxHp;
        this.turn = turn;
        this.turnEnded = turnEnded;
        this.bossHits = bossHits;
        this.bossDead = bossDead;
        this.partyWiped = partyWiped;
        this.bossSkillText = bossSkillText;
    }

    public String getMessage() { return message; }
    public int getDamage() { return damage; }
    public int getBossHp() { return bossHp; }
    public int getMaxHp() { return maxHp; }
    public int getTurn() { return turn; }
    public boolean isTurnEnded() { return turnEnded; }

    public List<RaidGameService.BossHit> getBossHits() {
        return bossHits;
    }

    public boolean isBossDead() { return bossDead; }
    public boolean isPartyWiped() { return partyWiped; }

    public String getBossSkillText() { return bossSkillText; }
}

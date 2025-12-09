package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BossService {

    private static final int DEFAULT_MAX_HP = 1000;

    // roomId -> BossState
    private final Map<String, BossState> bosses = new ConcurrentHashMap<>();

    /**
     * 해당 roomId의 보스를 가져오거나, 없으면 기본 체력으로 생성
     */
    public synchronized BossState getOrCreateBoss(String roomId) {
        BossState boss = bosses.get(roomId);
        if (boss == null) {
            boss = new BossState(roomId, DEFAULT_MAX_HP, DEFAULT_MAX_HP, false);
            bosses.put(roomId, boss);
        }
        return boss;
    }

    /**
     * 보스를 초기화 (admin에서 쓰거나, 레이드 시작 시 호출)
     */
    public synchronized BossState initBoss(String roomId, int maxHp) {
        BossState boss = new BossState(roomId, maxHp, maxHp, false);
        bosses.put(roomId, boss);
        return boss;
    }

    /**
     * roomId 기준으로 보스를 공격하고, 결과 문자열을 돌려줌.
     * damage가 null이거나 0 이하이면 기본 데미지 10 사용.
     */
    public synchronized String attackBoss(String roomId, String attacker, Integer damage) {
        if (roomId == null || roomId.isEmpty()) {
            roomId = "default";
        }

        BossState boss = getOrCreateBoss(roomId);

        if (boss.isDead()) {
            return "보스는 이미 쓰러져 있습니다. (방: " + roomId + ")";
        }

        int actualDamage = (damage != null && damage > 0) ? damage : 10; //대미지 입력..?
        int oldHp = boss.getHp();
        int newHp = Math.max(0, oldHp - actualDamage);
        boss.setHp(newHp);

        if (newHp == 0) {
            boss.setDead(true);
            return attacker + "이(가) 보스를 처치했습니다! (방: " + roomId +
                    ", 보스 HP: 0/" + boss.getMaxHp() + ")";
        }

        return attacker + "이(가) " + actualDamage + "의 피해를 입혔습니다. " +
                "(방: " + roomId + ", 보스 HP: " + newHp + "/" + boss.getMaxHp() + ")";
    }

    /**
     * 단순 조회용 (UI에 HP바 그릴 때 등)
     */
    public BossState getBossState(String roomId) {
        return bosses.get(roomId);
    }
}

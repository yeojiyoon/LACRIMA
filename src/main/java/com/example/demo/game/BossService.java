package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BossService {

    private static final int DEFAULT_MAX_HP = 1000;

    // roomId -> BossState (인메모리 상태)
    private final Map<String, BossState> bosses = new ConcurrentHashMap<>();

    // roomId -> BossMonster PK (DB상의 보스와 매핑)
    private final Map<String, Long> roomBossIds = new ConcurrentHashMap<>();

    private final BossMonsterRepository bossMonsterRepository;

    public BossService(BossMonsterRepository bossMonsterRepository) {
        this.bossMonsterRepository = bossMonsterRepository;
    }

    /**
     * roomId에 대해 DB 보스를 기반으로 보스 상태를 초기화.
     * - BossMonster의 maxHp / currentHp를 이용해 상태 구성
     * - roomBossIds에 roomId -> bossId 매핑 저장
     */
    public synchronized BossState initBoss(String roomId, BossMonster bossMonster) {
        int maxHp = (int) bossMonster.getMaxHp();
        int currentHp = (int) bossMonster.getCurrentHp();

        BossState state = new BossState(roomId, currentHp, maxHp, bossMonster.isDead());
        bosses.put(roomId, state);
        roomBossIds.put(roomId, bossMonster.getId());

        return state;
    }

    /**
     * 기존 기본 보스 초기화 (혹시 쓸 수도 있으니까 남겨둠)
     */
    public synchronized BossState initBoss(String roomId, int maxHp) {
        BossState boss = new BossState(roomId, maxHp, maxHp, false);
        bosses.put(roomId, boss);
        // roomBossIds에는 아무것도 안 넣음 (DB와 매핑되지 않은 보스)
        return boss;
    }

    /**
     * 해당 roomId의 보스를 가져오거나, 없으면 기본 체력으로 생성
     * (DB 매핑 없이도 돌아가야 하는 경우를 위한 fallback)
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
     * roomId 기준으로 보스를 공격하고, 결과 문자열을 돌려줌.
     * damage가 null이거나 0 이하이면 기본 데미지 10 사용.
     * 🔹 여기서 인메모리 HP 깎은 뒤, 매핑된 BossMonster.currentHp도 DB에 반영
     */
    public synchronized String attackBoss(String roomId, String attacker, Integer damage) {
        if (roomId == null || roomId.isEmpty()) {
            roomId = "default";
        }

        BossState boss = getOrCreateBoss(roomId);

        if (boss.isDead()) {
            return "보스는 이미 쓰러져 있습니다. (방: " + roomId + ")";
        }

        int actualDamage = (damage != null && damage > 0) ? damage : 10;
        int oldHp = boss.getHp();
        int newHp = Math.max(0, oldHp - actualDamage);
        boss.setHp(newHp);

        // 🔹 DB BossMonster에도 currentHp 반영 (roomId -> bossId 매핑이 있는 경우에만)
        Long bossId = roomBossIds.get(roomId);
        if (bossId != null) {
            Optional<BossMonster> opt = bossMonsterRepository.findById(bossId);
            if (opt.isPresent()) {
                BossMonster entity = opt.get();
                entity.setCurrentHp(newHp);
                if (newHp == 0) {
                    // 필요하면 isDead 같은 flag를 BossMonster에도 두고 갱신 가능
                    // entity.setDead(true);
                }
                bossMonsterRepository.save(entity);
            }
        }

        if (newHp == 0) {
            boss.setDead(true);
            return attacker + "이(가) 보스를 처치했습니다!";  // ✅ 방/HP 정보 빼기
        }

        return attacker + "이(가) " + actualDamage + "의 피해를 입혔습니다.";
    }

    /**
     * 단순 조회용 (UI에 HP바 그릴 때 등)
     */
    public BossState getBossState(String roomId) {
        return bosses.get(roomId);
    }
}

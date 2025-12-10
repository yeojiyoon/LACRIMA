package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class BossSkillExecutor {

    private final Random random = new Random();

    /**
     * 보스 턴마다 RaidGameService에서 호출해 줄 메서드.
     *
     * @param boss       스킬을 사용하는 보스
     * @param skill      DB에서 꺼내온 BossSkill (name, description, effectText, useText 포함)
     * @param context    현재 전투 컨텍스트
     * @param turnNumber 현재 턴 번호 (1부터 시작한다고 가정)
     */
    public void executeBossSkill(BossMonster boss,
                                 BossSkill skill,
                                 RaidBattleContext context,
                                 int turnNumber) {

        // 1) 스킬 사용 텍스트 브로드캐스트
        if (skill.getUseText() != null && !skill.getUseText().isBlank()) {
            context.broadcastSystemMessage(skill.getUseText());
        }

        // 2) 스킬 코드(name) 기준으로 분기
        String code = skill.getName(); // 지금은 name을 코드처럼 쓰고 있다고 가정: breath, highattack ...

        switch (code) {
            case "breath" -> executeBreath(boss, context);
            case "highattack" -> executeHighAttack(boss, context, turnNumber);
            default -> context.broadcastSystemMessage("[ERROR] 알 수 없는 보스 스킬 코드: " + code);
        }
    }

    /**
     * breath : 2인 3d5 공격
     * - 살아있는 파티원 중 랜덤 2명에게 각각 3d5 데미지
     */
    private void executeBreath(BossMonster boss, RaidBattleContext context) {
        List<PlayerCharacter> alive = context.getAliveMembers();
        if (alive.isEmpty()) {
            return;
        }

        // 파티원 순서 섞고 앞에서 2명까지 타겟으로 사용
        Collections.shuffle(alive, random);

        int targetCount = Math.min(2, alive.size());
        for (int i = 0; i < targetCount; i++) {
            PlayerCharacter target = alive.get(i);
            long damage = roll3d5();
            context.applyDamage(target, damage, "브레스");
        }
    }

    /**
     * highattack : 2턴마다 1인에게 40의 고정 데미지
     * - 짝수 턴에서만 발동한다고 가정 (turnNumber % 2 == 0)
     */
    private void executeHighAttack(BossMonster boss,
                                   RaidBattleContext context,
                                   int turnNumber) {

        // 홀수턴이면 아직 차지 중이라고 보고 스킵
        if (turnNumber % 2 != 0) {
            // 필요하면 "차지 중" 같은 메시지 내보내도 됨
            return;
        }

        List<PlayerCharacter> alive = context.getAliveMembers();
        if (alive.isEmpty()) {
            return;
        }

        // 무작위 1인
        PlayerCharacter target = alive.get(random.nextInt(alive.size()));

        long damage = 40; // 고정 데미지
        context.applyDamage(target, damage, "강공");
    }

    /**
     * 3d5 : 1~5 사이의 값을 3번 굴려 합산
     */
    private long roll3d5() {
        long sum = 0;
        for (int i = 0; i < 3; i++) {
            // nextInt(5) -> 0~4 이므로 +1 해서 1~5
            sum += (random.nextInt(5) + 1);
        }
        return sum;
    }
}

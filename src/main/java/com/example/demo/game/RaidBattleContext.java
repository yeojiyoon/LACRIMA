package com.example.demo.game;

import java.util.List;

/**
 * 보스 스킬이 전투에 영향을 줄 때 사용하는 최소한의 인터페이스.
 * 실제 구현체는 RaidGameService 쪽에서 만들고 넘겨주면 됨.
 */
public interface RaidBattleContext {

    /** 현재 살아 있는 파티원 목록 (죽은 캐릭터는 제외) */
    List<PlayerCharacter> getAliveMembers();

    /** 대상에게 고정 데미지를 적용 */
    void applyDamage(PlayerCharacter target, long damage, String reason);

    /** 시스템/연출 메시지 브로드캐스트 */
    void broadcastSystemMessage(String message);
}

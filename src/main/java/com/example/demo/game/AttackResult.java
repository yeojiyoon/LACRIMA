// package com.example.demo.game;
package com.example.demo.game;

//게임 결과 DTO
//아마 나중에 player hp도 넣어야 할듯
public class AttackResult {

    private final String message;  // "누가 몇 데미지!" 같은 설명 문자열
    private final Integer bossHp;  // null일 수도 있게 Integer
    private final Integer maxHp;
    private final Integer damage;

    public AttackResult(String message, Integer bossHp, Integer maxHp, Integer damage) {
        this.message = message;
        this.bossHp = bossHp;
        this.maxHp = maxHp;
        this.damage = damage;
    }

    public String getMessage() {
        return message;
    }

    public Integer getBossHp() {
        return bossHp;
    }

    public Integer getMaxHp() {
        return maxHp;
    }

    public Integer getDamage() {
        return damage;
    }
}

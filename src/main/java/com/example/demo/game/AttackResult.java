package com.example.demo.game;

public class AttackResult {

    private final String message;   // 로그 텍스트
    private final int damage;       // 이번 공격으로 입힌 데미지
    private final int bossHp;       // 보스 현재 HP
    private final int maxHp;        // 보스 최대 HP

    private final int turn;         // 현재 턴 번호
    private final boolean turnEnded; // 이 공격으로 플레이어 턴이 끝났는지

    public AttackResult(String message,
                        int damage,
                        int bossHp,
                        int maxHp,
                        int turn,
                        boolean turnEnded) {
        this.message = message;
        this.damage = damage;
        this.bossHp = bossHp;
        this.maxHp = maxHp;
        this.turn = turn;
        this.turnEnded = turnEnded;
    }

    public String getMessage() { return message; }
    public int getDamage() { return damage; }
    public int getBossHp() { return bossHp; }
    public int getMaxHp() { return maxHp; }
    public int getTurn() { return turn; }
    public boolean isTurnEnded() { return turnEnded; }
}

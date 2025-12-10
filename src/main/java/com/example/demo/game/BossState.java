package com.example.demo.game;

public class BossState {

    private String roomId;
    private int maxHp;
    private int hp;
    private boolean dead;

    public BossState() {
    }

    public BossState(String roomId, int hp, int maxHp, boolean dead) {
        this.roomId = roomId;
        this.hp = hp;
        this.maxHp = maxHp;
        this.dead = dead;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    @Override
    public String toString() {
        return "BossState{" +
                "roomId='" + roomId + '\'' +
                ", maxHp=" + maxHp +
                ", hp=" + hp +
                ", dead=" + dead +
                '}';
    }
}

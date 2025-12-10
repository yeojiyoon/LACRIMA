package com.example.demo.websocket;

import com.example.demo.game.PartyMemberView;

import java.util.List;

public class ChatMessage {

    private MessageType type;
    private String roomId;
    private String sender;
    private String message;

    private Integer damage;
    private Integer bossHp;
    private Integer maxHp;
    private Integer defense;

    private String timestamp;
    private String comment;
    private Integer turn;
    private String command;    // ADMIN 메시지용 (ex: START_BATTLE)

    private List<PartyMemberView> party;

    private Long targetCharacterId;

    // 🔥 보스 공격 로그용
    private String targetName;
    private Integer targetHp;
    private Integer targetMaxHp;

    public ChatMessage() {}

    public ChatMessage(MessageType type, String roomId, String sender, String message,
                       Integer damage, Integer bossHp, Integer maxHp, String timestamp) {
        this.type = type;
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
        this.damage = damage;
        this.bossHp = bossHp;
        this.maxHp = maxHp;
        this.timestamp = timestamp;
    }

    // ===== Getter / Setter =====

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getTargetCharacterId() {
        return targetCharacterId;
    }

    public void setTargetCharacterId(Long targetCharacterId) {
        this.targetCharacterId = targetCharacterId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getDamage() {
        return damage;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public Integer getBossHp() {
        return bossHp;
    }

    public void setBossHp(Integer bossHp) {
        this.bossHp = bossHp;
    }

    public Integer getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(Integer maxHp) {
        this.maxHp = maxHp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getDefense() {
        return defense;
    }

    public void setDefense(Integer defense) {
        this.defense = defense;
    }

    public List<PartyMemberView> getParty() { return party; }
    public void setParty(List<PartyMemberView> party) { this.party = party; }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // 🔥 보스 공격 표시에 쓰일 필드들
    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Integer getTargetHp() {
        return targetHp;
    }

    public void setTargetHp(Integer targetHp) {
        this.targetHp = targetHp;
    }

    public Integer getTargetMaxHp() {
        return targetMaxHp;
    }

    public void setTargetMaxHp(Integer targetMaxHp) {
        this.targetMaxHp = targetMaxHp;
    }

    public Integer getTurn() {
        return turn;
    }

    public void setTurn(Integer turn) {
        this.turn = turn;
    }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    // ===== toString (디버깅용) =====
    @Override
    public String toString() {
        return "ChatMessage{" +
                "type=" + type +
                ", roomId='" + roomId + '\'' +
                ", sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                ", damage=" + damage +
                ", bossHp=" + bossHp +
                ", maxHp=" + maxHp +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}

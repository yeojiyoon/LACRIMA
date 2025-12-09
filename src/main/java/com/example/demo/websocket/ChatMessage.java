package com.example.demo.websocket;

public class ChatMessage {

    private MessageType type;
    private String roomId;
    private String sender;
    private String message;
    private Integer damage;
    private Integer bossHp;
    private Integer maxHp;
    private String timestamp;

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

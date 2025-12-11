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


    // 🔥 보스 스킬 쿨타임 UI용 필드
    private Integer skill1CdNow;
    private Integer skill1CdMax;
    private Boolean skill1Available;

    private Integer skill2CdNow;
    private Integer skill2CdMax;
    private Boolean skill2Available;

    private Integer skill3CdNow;
    private Integer skill3CdMax;
    private Boolean skill3Available;

    private String skill1Name;
    private String skill1Desc;

    private String skill2Name;
    private String skill2Desc;

    private String skill3Name;
    private String skill3Desc;

    public Integer getSkill1CdNow() { return skill1CdNow; }
    public void setSkill1CdNow(Integer skill1CdNow) { this.skill1CdNow = skill1CdNow; }

    public Integer getSkill1CdMax() { return skill1CdMax; }
    public void setSkill1CdMax(Integer skill1CdMax) { this.skill1CdMax = skill1CdMax; }

    public Boolean getSkill1Available() { return skill1Available; }
    public void setSkill1Available(Boolean skill1Available) { this.skill1Available = skill1Available; }

    public Integer getSkill2CdNow() { return skill2CdNow; }
    public void setSkill2CdNow(Integer skill2CdNow) { this.skill2CdNow = skill2CdNow; }

    public Integer getSkill2CdMax() { return skill2CdMax; }
    public void setSkill2CdMax(Integer skill2CdMax) { this.skill2CdMax = skill2CdMax; }

    public Boolean getSkill2Available() { return skill2Available; }
    public void setSkill2Available(Boolean skill2Available) { this.skill2Available = skill2Available; }

    public Integer getSkill3CdNow() { return skill3CdNow; }
    public void setSkill3CdNow(Integer skill3CdNow) { this.skill3CdNow = skill3CdNow; }

    public Integer getSkill3CdMax() { return skill3CdMax; }
    public void setSkill3CdMax(Integer skill3CdMax) { this.skill3CdMax = skill3CdMax; }

    public Boolean getSkill3Available() { return skill3Available; }
    public void setSkill3Available(Boolean skill3Available) { this.skill3Available = skill3Available; }

    public String getSkill1Name() { return skill1Name; }
    public void setSkill1Name(String skill1Name) { this.skill1Name = skill1Name; }

    public String getSkill1Desc() { return skill1Desc; }
    public void setSkill1Desc(String skill1Desc) { this.skill1Desc = skill1Desc; }

    public String getSkill2Name() { return skill2Name; }
    public void setSkill2Name(String skill2Name) { this.skill2Name = skill2Name; }

    public String getSkill2Desc() { return skill2Desc; }
    public void setSkill2Desc(String skill2Desc) { this.skill2Desc = skill2Desc; }

    public String getSkill3Name() { return skill3Name; }
    public void setSkill3Name(String skill3Name) { this.skill3Name = skill3Name; }

    public String getSkill3Desc() { return skill3Desc; }
    public void setSkill3Desc(String skill3Desc) { this.skill3Desc = skill3Desc; }


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

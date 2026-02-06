package com.lacrima.lacrimademo.explore.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "explore_event")
public class ExploreEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code; // E1001

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer eventType; // 1=SCENE,2=TREASURE,3=MONSTER (일단 int로 빠르게)

    @Column(nullable = false)
    private boolean repeatable;

    @Column(nullable = false)
    private boolean active = true;

    // VN assets
    private String backgroundUrl;
    private String npcName;
    private String npcPortraitUrl;

    @Column(columnDefinition = "text")
    private String scriptJson; // TEXT로 저장

    @Column(columnDefinition = "text")
    private String rewardJson; // TEXT로 저장

    protected ExploreEvent() {}

    public ExploreEvent(String code, String name, Integer eventType, boolean repeatable) {
        this.code = code;
        this.name = name;
        this.eventType = eventType;
        this.repeatable = repeatable;
    }

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Integer getEventType() { return eventType; }
    public boolean isRepeatable() { return repeatable; }
    public boolean isActive() { return active; }

    public String getBackgroundUrl() { return backgroundUrl; }
    public String getNpcName() { return npcName; }
    public String getNpcPortraitUrl() { return npcPortraitUrl; }
    public String getScriptJson() { return scriptJson; }
    public String getRewardJson() { return rewardJson; }

    public void setName(String name) { this.name = name; }
    public void setEventType(Integer eventType) { this.eventType = eventType; }
    public void setRepeatable(boolean repeatable) { this.repeatable = repeatable; }
    public void setActive(boolean active) { this.active = active; }

    public void setBackgroundUrl(String backgroundUrl) { this.backgroundUrl = backgroundUrl; }
    public void setNpcName(String npcName) { this.npcName = npcName; }
    public void setNpcPortraitUrl(String npcPortraitUrl) { this.npcPortraitUrl = npcPortraitUrl; }
    public void setScriptJson(String scriptJson) { this.scriptJson = scriptJson; }
    public void setRewardJson(String rewardJson) { this.rewardJson = rewardJson; }
}

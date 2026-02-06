package com.lacrima.lacrimademo.explore.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "map_tile")
@IdClass(MapTileKey.class)
public class MapTile {
    @Id @Column(nullable = false)
    private Long mapId;

    @Id private int x;
    @Id private int y;

    private boolean discovered;
    private boolean isBlocked; //벽인지 아닌지

    @Column(name = "event_type")
    private Integer eventType;

    @Column(name = "event_id")
    private Integer eventId;

    protected MapTile() {}

    public MapTile(Long mapId, int x, int y) {
        this.mapId = mapId;
        this.x = x;
        this.y = y;
        this.discovered = false;
        this.isBlocked = false;
        this.eventType = null;
        this.eventId = null;
    }

    public Long getMapId() { return mapId; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isDiscovered() { return discovered; }
    public void setDiscovered(boolean discovered) { this.discovered = discovered; }
    public boolean getisBlocked() { return isBlocked; }
    public void setisBlocked(boolean isBlocked) { this.isBlocked = isBlocked; }
    public Integer getEventType() { return eventType; }
    public Integer getEventId() { return eventId; }

    public boolean hasEvent() { return eventType != null && eventId != null; }

    public void setEvent(Integer eventType, Integer eventId) {
        this.eventType = eventType;
        this.eventId = eventId;
    }

    public void clearEvent() {
        this.eventType = null;
        this.eventId = null;
    }

}

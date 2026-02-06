package com.lacrima.lacrimademo.explore.web.dto;

import com.lacrima.lacrimademo.explore.domain.MapTile;

public record TileDto(int x, int y, boolean discovered, Integer eventType, Integer eventId) {
    public static TileDto from(MapTile t) {
        return new TileDto(t.getX(), t.getY(), t.isDiscovered(), t.getEventType(), t.getEventId());
    }
}

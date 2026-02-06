package com.lacrima.lacrimademo.explore.domain;

import java.io.Serializable;
import java.util.Objects;

public class MapTileKey implements Serializable {
    private Long mapId;
    private int x;
    private int y;

    public MapTileKey() {}
    public MapTileKey(Long mapId, int x, int y) {
        this.mapId = mapId;
        this.x = x;
        this.y = y;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapTileKey)) return false;
        MapTileKey k = (MapTileKey) o;
        return x == k.x && y == k.y && Objects.equals(mapId, k.mapId);
    }

    @Override public int hashCode() {
        return Objects.hash(mapId, x, y);
    }
}

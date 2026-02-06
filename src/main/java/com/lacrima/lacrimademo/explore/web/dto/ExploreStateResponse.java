package com.lacrima.lacrimademo.explore.web.dto;

import com.lacrima.lacrimademo.explore.domain.ExplorationState;
import com.lacrima.lacrimademo.explore.domain.ExploreMap;

import java.util.List;

public record ExploreStateResponse(
        int ap,
        int posX,
        int posY,
        boolean started,
        int mapWidth,
        int mapHeight,
        List<TileDto> tiles,
        Integer lastEventType,
        Integer lastEventId
) {
    public static ExploreStateResponse from(
            ExplorationState st,
            ExploreMap map,
            List<TileDto> tiles,
            Integer eventType,
            Integer eventId
    ) {
        return new ExploreStateResponse(
                st.getAp(),
                st.getPosX(),
                st.getPosY(),
                !(st.getPosX() == 0 && st.getPosY() == 0),
                map.getWidth(),
                map.getHeight(),
                tiles,
                eventType,
                eventId
        );
    }
}


package com.lacrima.lacrimademo.admin.service.explore;

import com.lacrima.lacrimademo.explore.domain.*;
import com.lacrima.lacrimademo.explore.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminExploreMapService {

    private final ExploreMapRepository mapRepo;
    private final MapTileRepository tileRepo;
    private final ExploreEventRepository eventRepo;

    public AdminExploreMapService(ExploreMapRepository mapRepo,
                                  MapTileRepository tileRepo,
                                  ExploreEventRepository eventRepo) {
        this.mapRepo = mapRepo;
        this.tileRepo = tileRepo;
        this.eventRepo = eventRepo;
    }

    public List<ExploreMap> listMaps() {
        return mapRepo.findAll();
    }

    @Transactional
    public ExploreMap createMap(String code, String name, int width, int height, String explanation) {
        // TODO: validate unique code
        ExploreMap map = mapRepo.save(new ExploreMap(code, name, width, height, explanation));

        // 625 타일 생성 (현재 설계 유지)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tileRepo.save(new MapTile(map.getId(), x, y));
            }
        }
        return map;
    }

    public ExploreMap getMap(Long mapId) {
        return mapRepo.findById(mapId).orElseThrow();
    }

    public List<ExploreEvent> listActiveEvents() {
        return eventRepo.findByActiveTrueOrderByNameAsc();
    }

    public MapTile getTile(Long mapId, int x, int y) {
        return tileRepo.findByMapIdAndXAndY(mapId, x, y).orElseThrow();
    }

    public List<MapTile> listTiles(Long mapId) {
        return tileRepo.findAllByMapId(mapId);
    }

    @Transactional
    public void updateTile(Long mapId, int x, int y, boolean blocked, Integer eventType, Integer eventId) {
        MapTile t = getTile(mapId, x, y);
        t.setisBlocked(blocked);

        if (eventId == null || eventType == null) {
            t.clearEvent();
        } else {
            // TODO: validate event exists
            t.setEvent(eventType, eventId);
        }
        // JPA dirty checking
    }

    public Optional<ExploreEvent> findEvent(Integer eventId) {
        if (eventId == null) return Optional.empty();
        return eventRepo.findById(eventId);
    }
}

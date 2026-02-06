package com.lacrima.lacrimademo.explore.service;

import com.lacrima.lacrimademo.explore.domain.ExplorationState;
import com.lacrima.lacrimademo.explore.domain.ExploreMap;
import com.lacrima.lacrimademo.explore.domain.MapTile;
import com.lacrima.lacrimademo.explore.repo.ExplorationStateRepository;
import com.lacrima.lacrimademo.explore.repo.ExploreMapRepository;
import com.lacrima.lacrimademo.explore.repo.MapTileRepository;
import com.lacrima.lacrimademo.explore.web.dto.TileDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExploreService {

    /*------------------레포지토리 객체 생성-----------------*/

    private final ExplorationStateRepository stateRepo;
    private final MapTileRepository tileRepo;
    private final ExploreMapRepository mapRepo;

    public ExploreService(ExplorationStateRepository stateRepo,
                          MapTileRepository tileRepo,
                          ExploreMapRepository mapRepo) {
        this.stateRepo = stateRepo;
        this.tileRepo = tileRepo;
        this.mapRepo = mapRepo;
    }

    /*---------------------------------------------------*/

    @Transactional
    public void ensureMapInitialized(long mapId) {
        ExploreMap map = getMap(mapId);

        if (!tileRepo.findAllByMapId(mapId).isEmpty()) return;

        List<MapTile> tiles = new ArrayList<>();
        for (int y = 1; y <= map.getHeight(); y++) {
            for (int x = 1; x <= map.getWidth(); x++) {
                tiles.add(new MapTile(mapId, x, y));
            }
        }
        tileRepo.saveAll(tiles);
    }

    @Transactional(readOnly = true)
    public ExploreMap getMapForResponse(long mapId) {
        return getMap(mapId);
    }

    @Transactional
    public ExplorationState getOrCreateState(long characterId) { //캐릭터 탐색 정보
        return stateRepo.findById(characterId)
                .orElseGet(() -> stateRepo.save(new ExplorationState(characterId, 10, 0, 0)));
    }

    @Transactional(readOnly = true)
    public List<TileDto> getTiles(long mapId) {
        return tileRepo.findAllByMapId(mapId).stream()
                .map(TileDto::from)
                .toList();
    }

    @Transactional
    public ExplorationState start(long mapId, long characterId, int x, int y) {
        ensureMapInitialized(mapId);
        ExploreMap map = getMap(mapId);
        validateBounds(map, x, y);

        ExplorationState st = getOrCreateState(characterId);

        if (!(st.getPosX() == 0 && st.getPosY() == 0)) {
            throw new IllegalStateException("이미 시작한 상태입니다.");
        }

        MapTile tile = tileRepo.findByMapIdAndXAndY(mapId, x, y)
                .orElseThrow(() -> new IllegalStateException("타일이 없습니다."));

        if (tile.getisBlocked()) throw new IllegalStateException("벽(이동 불가)입니다.");

        st.setPosX(x);
        st.setPosY(y);

        if (!tile.isDiscovered()) tile.setDiscovered(true);

        return st;
    }


    @Transactional
    public MoveResult move(long mapId, long characterId, int x, int y) {
        ensureMapInitialized(mapId);
        ExploreMap map = getMap(mapId);
        validateBounds(map, x, y);

        ExplorationState st = getOrCreateState(characterId);

        if (st.getPosX() == 0 && st.getPosY() == 0) throw new IllegalStateException("시작점을 먼저 선택하세요.");
        if (!isAdjacent(st.getPosX(), st.getPosY(), x, y)) throw new IllegalArgumentException("인접 타일로만 이동할 수 있습니다.");
        if (st.getAp() <= 0) throw new IllegalStateException("AP가 부족합니다.");

        MapTile tile = tileRepo.findByMapIdAndXAndY(mapId, x, y)
                .orElseThrow(() -> new IllegalStateException("타일이 없습니다."));

        if (tile.getisBlocked()) throw new IllegalStateException("벽(이동 불가)입니다.");

        int cost = 0;

        if(tile.isDiscovered()) {
            st.setOpenStepAcc(st.getOpenStepAcc() - 1);
            if (st.getOpenStepAcc() == 0) {
                st.setOpenStepAcc(3);
                cost = 1;
            }
        }

        if (!tile.isDiscovered()) {
            tile.setDiscovered(true);
            cost = 1;
        }

        if (st.getAp() < cost) throw new IllegalStateException("AP가 부족합니다.");

        st.setAp(st.getAp() - cost);
        st.setPosX(x);
        st.setPosY(y);

        return new MoveResult(st, tile.getEventType(), tile.getEventId());
    }

    public record MoveResult(ExplorationState state, Integer eventType, Integer eventId) {}

    private void validateBounds(ExploreMap map, int x, int y) {
        if (x < 1 || x > map.getWidth() || y < 1 || y > map.getHeight()) {
            throw new IllegalArgumentException("맵 범위 밖입니다.");
        }
    }


    private boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) == 1;
    }

    private ExploreMap getMap(long mapId) {
        return mapRepo.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("맵이 없습니다. admin에서 먼저 생성하세요."));
    }
}

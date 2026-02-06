package com.lacrima.lacrimademo.explore;


import com.lacrima.lacrimademo.explore.domain.MapTile;
import com.lacrima.lacrimademo.explore.repo.MapTileRepository;
import com.lacrima.lacrimademo.explore.domain.ExplorationState;
import com.lacrima.lacrimademo.explore.repo.ExplorationStateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MapInitRunner implements CommandLineRunner {

    private final MapTileRepository tileRepo;
    private final ExplorationStateRepository explorationStateRepository;

    public MapInitRunner(
            MapTileRepository tileRepo,
            ExplorationStateRepository explorationStateRepository
    ) {
        this.tileRepo = tileRepo;
        this.explorationStateRepository = explorationStateRepository;
    }

    @Override
    public void run(String... args) {
        long mapId = 1L;
        Long characterId = 1L;

        // 이미 있으면 건너뛰기
        if (!tileRepo.findAllByMapId(mapId).isEmpty()) return;

        List<MapTile> tiles = new ArrayList<>();
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                tiles.add(new MapTile(mapId, x, y));
            }
        }
        tileRepo.saveAll(tiles);

        if (explorationStateRepository.existsById(characterId)) {
            return;
        }

        ExplorationState state =
                new ExplorationState(characterId, 10, 0, 0);

        explorationStateRepository.save(state);
    }
}


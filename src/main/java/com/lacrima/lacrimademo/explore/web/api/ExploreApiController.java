package com.lacrima.lacrimademo.explore.web.api;

import com.lacrima.lacrimademo.explore.domain.ExplorationState;
import com.lacrima.lacrimademo.explore.domain.ExploreMap;
import com.lacrima.lacrimademo.explore.service.ExploreService;
import com.lacrima.lacrimademo.explore.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/explore")
public class ExploreApiController {

    private final ExploreService exploreService;

    public ExploreApiController(ExploreService exploreService) {
        this.exploreService = exploreService;
    }

    @GetMapping("/state")
    public ExploreStateResponse state(
            @RequestParam long mapId,
            @RequestParam long characterId
    ) {
        exploreService.ensureMapInitialized(mapId);

        ExplorationState st = exploreService.getOrCreateState(characterId);
        List<TileDto> tiles = exploreService.getTiles(mapId);
        ExploreMap map = exploreService.getMapForResponse(mapId);

        return ExploreStateResponse.from(st, map, tiles, null, null);
    }

    @PostMapping("/start")
    public ExploreStateResponse start(
            @RequestParam long mapId,
            @RequestParam long characterId,
            @RequestBody StartRequest req
    ) {
        ExplorationState st = exploreService.start(mapId, characterId, req.x(), req.y());
        List<TileDto> tiles = exploreService.getTiles(mapId);
        ExploreMap map = exploreService.getMapForResponse(mapId);

        return ExploreStateResponse.from(st, map, tiles, null, null);
    }


    @PostMapping("/move")
    public ExploreStateResponse move(
            @RequestParam long mapId,
            @RequestParam long characterId,
            @RequestBody MoveRequest req
    ) {
        ExploreService.MoveResult r =
                exploreService.move(mapId, characterId, req.x(), req.y());

        List<TileDto> tiles = exploreService.getTiles(mapId);
        ExploreMap map = exploreService.getMapForResponse(mapId);

        return ExploreStateResponse.from(
                r.state(),
                map,
                tiles,
                r.eventType(),
                r.eventId()
        );
    }

}

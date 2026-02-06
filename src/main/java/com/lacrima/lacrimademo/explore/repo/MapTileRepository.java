package com.lacrima.lacrimademo.explore.repo;

import com.lacrima.lacrimademo.explore.domain.MapTile;
import com.lacrima.lacrimademo.explore.domain.MapTileKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MapTileRepository extends JpaRepository<MapTile, MapTileKey> {
    List<MapTile> findAllByMapId(Long mapId);
    Optional<MapTile> findByMapIdAndXAndY(Long mapId, int x, int y);
}
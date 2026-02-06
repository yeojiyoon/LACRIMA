package com.lacrima.lacrimademo.admin.controller.explore;

import com.lacrima.lacrimademo.admin.service.explore.AdminExploreMapService;
import com.lacrima.lacrimademo.explore.domain.ExploreEvent;
import com.lacrima.lacrimademo.explore.domain.ExploreMap;
import com.lacrima.lacrimademo.explore.domain.MapTile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class AdminExploreMapController {

    private final AdminExploreMapService svc;

    public AdminExploreMapController(AdminExploreMapService svc) {
        this.svc = svc;
    }

    @GetMapping("/admin/explore/maps")
    public String maps(Model model) {
        model.addAttribute("maps", svc.listMaps());
        return "admin/explore/map_list";
    }

    @PostMapping("/admin/explore/maps")
    public String createMap(@RequestParam String code,
                            @RequestParam String name,
                            @RequestParam(defaultValue = "25") int width,
                            @RequestParam(defaultValue = "25") int height,
                            @RequestParam(required = false) String explanation) {
        svc.createMap(code, name, width, height, explanation);
        return "redirect:/admin/explore/maps";
    }

    @GetMapping("/admin/explore/maps/{mapId}")
    public String mapDetail(@PathVariable Long mapId, Model model) {
        ExploreMap map = svc.getMap(mapId);
        model.addAttribute("map", map);
        model.addAttribute("events", svc.listActiveEvents());
        return "admin/explore/map_detail";
    }

    // (선택) 맵에 이벤트 배치된 타일들만 표시용: 초기 페인트
    @ResponseBody
    @GetMapping("/admin/explore/api/placements")
    public List<Map<String, Object>> placements(@RequestParam Long mapId) {
        List<MapTile> tiles = svc.listTiles(mapId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (MapTile t : tiles) {
            if (t.hasEvent()) {
                out.add(Map.of("x", t.getX(), "y", t.getY(), "eventId", t.getEventId(), "eventType", t.getEventType()));
            }
        }
        return out;
    }

    // 우측 패널용 타일 조회 + 이벤트 프리뷰 포함
    @ResponseBody
    @GetMapping("/admin/explore/api/tile")
    public Map<String, Object> tile(@RequestParam Long mapId, @RequestParam int x, @RequestParam int y) {
        MapTile t = svc.getTile(mapId, x, y);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("mapId", mapId);
        res.put("x", x);
        res.put("y", y);
        res.put("blocked", t.getisBlocked());
        res.put("hasEvent", t.hasEvent());
        res.put("eventType", t.getEventType());
        res.put("eventId", t.getEventId());

        // 이벤트가 있으면 프리뷰 데이터 붙이기
        if (t.hasEvent()) {
            svc.findEvent(t.getEventId()).ifPresent(e -> res.put("eventPreview", eventPreview(e)));
        }
        return res;
    }

    private Map<String, Object> eventPreview(ExploreEvent e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("code", e.getCode());
        m.put("name", e.getName());
        m.put("eventType", e.getEventType());

        // null 가능 필드들은 그대로 넣어도 됨 (LinkedHashMap은 OK)
        m.put("backgroundUrl", e.getBackgroundUrl());
        m.put("npcName", e.getNpcName());
        m.put("npcPortraitUrl", e.getNpcPortraitUrl());
        m.put("scriptPreview", preview(e.getScriptJson()));

        return m;
    }


    private String preview(String s) {
        if (s == null || s.isBlank()) return "";
        return s.length() > 160 ? s.substring(0, 160) + "..." : s;
    }

    // 저장/삭제
    @ResponseBody
    @PostMapping("/admin/explore/api/tile")
    public Map<String, Object> updateTile(@RequestBody Map<String, Object> body) {
        Long mapId = ((Number) body.get("mapId")).longValue();
        int x = ((Number) body.get("x")).intValue();
        int y = ((Number) body.get("y")).intValue();

        boolean blocked = body.get("blocked") != null && (Boolean) body.get("blocked");
        Integer eventType = body.get("eventType") == null ? null : ((Number) body.get("eventType")).intValue();
        Integer eventId = body.get("eventId") == null ? null : ((Number) body.get("eventId")).intValue();

        svc.updateTile(mapId, x, y, blocked, eventType, eventId);
        return Map.of("ok", true, "message", "저장 완료");
    }
}

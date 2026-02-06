package com.lacrima.lacrimademo.admin.service.explore;

import com.lacrima.lacrimademo.explore.domain.ExploreEvent;
import com.lacrima.lacrimademo.explore.repo.ExploreEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminExploreEventService {

    private final ExploreEventRepository repo;

    public AdminExploreEventService(ExploreEventRepository repo) {
        this.repo = repo;
    }

    public List<ExploreEvent> list() {
        return repo.findAll();
    }

    public ExploreEvent get(Integer id) {
        return repo.findById(id).orElseThrow();
    }

    @Transactional
    public ExploreEvent create(ExploreEvent e) {
        // TODO: validate code uniqueness
        return repo.save(e);
    }

    @Transactional
    public void update(Integer id, String name, Integer eventType, boolean repeatable, boolean active,
                       String backgroundUrl, String npcName, String npcPortraitUrl,
                       String scriptJson, String rewardJson) {

        ExploreEvent e = get(id);
        e.setName(name);
        e.setEventType(eventType);
        e.setRepeatable(repeatable);
        e.setActive(active);
        e.setBackgroundUrl(backgroundUrl);
        e.setNpcName(npcName);
        e.setNpcPortraitUrl(npcPortraitUrl);
        e.setScriptJson(scriptJson);
        e.setRewardJson(rewardJson);
    }
}

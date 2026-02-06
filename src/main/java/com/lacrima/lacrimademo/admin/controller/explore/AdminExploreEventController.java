package com.lacrima.lacrimademo.admin.controller.explore;

import com.lacrima.lacrimademo.admin.service.explore.AdminExploreEventService;
import com.lacrima.lacrimademo.explore.domain.ExploreEvent;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminExploreEventController {

    private final AdminExploreEventService svc;

    public AdminExploreEventController(AdminExploreEventService svc) {
        this.svc = svc;
    }

    @GetMapping("/admin/explore/events")
    public String list(Model model) {
        model.addAttribute("events", svc.list());
        return "admin/explore/event_list";
    }

    @GetMapping("/admin/explore/events/new")
    public String newForm(Model model) {
        model.addAttribute("event", new ExploreEvent("E0000", "새 이벤트", 1, false));
        model.addAttribute("isNew", true);
        return "admin/explore/event_form";
    }

    @PostMapping("/admin/explore/events")
    public String create(@RequestParam String code,
                         @RequestParam String name,
                         @RequestParam Integer eventType,
                         @RequestParam(defaultValue = "false") boolean repeatable) {

        svc.create(new ExploreEvent(code, name, eventType, repeatable));
        return "redirect:/admin/explore/events";
    }

    @GetMapping("/admin/explore/events/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("event", svc.get(id));
        model.addAttribute("isNew", false);
        return "admin/explore/event_form";
    }

    @PostMapping("/admin/explore/events/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam String name,
                         @RequestParam Integer eventType,
                         @RequestParam(defaultValue = "false") boolean repeatable,
                         @RequestParam(defaultValue = "true") boolean active,
                         @RequestParam(required = false) String backgroundUrl,
                         @RequestParam(required = false) String npcName,
                         @RequestParam(required = false) String npcPortraitUrl,
                         @RequestParam(required = false) String scriptJson,
                         @RequestParam(required = false) String rewardJson) {

        svc.update(id, name, eventType, repeatable, active, backgroundUrl, npcName, npcPortraitUrl, scriptJson, rewardJson);
        return "redirect:/admin/explore/events/" + id;
    }
}

package com.lacrima.lacrimademo.explore.web.page;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lacrima/dungeon")
public class DungeonPageController {

    @GetMapping
    public String dungeon() {
        return "explore/dungeon";
    }
}
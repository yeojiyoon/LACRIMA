package com.example.demo.admin;

import com.example.demo.game.BossSkill;
import com.example.demo.game.BossSkillRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/skills")
public class BossSkillAdminController {

    private final BossSkillRepository bossSkillRepository;

    public BossSkillAdminController(BossSkillRepository bossSkillRepository) {
        this.bossSkillRepository = bossSkillRepository;
    }

    // 스킬 목록
    @GetMapping
    public String list(Model model) {
        model.addAttribute("skills", bossSkillRepository.findAll());
        // 🔽 템플릿: src/main/resources/templates/admin/skills/skill-list.html
        return "admin/skills/skill-list";
    }

    // 신규 스킬 등록 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("skill", new BossSkill());
        model.addAttribute("formAction", "/admin/skills/new");
        // 🔽 폼 템플릿
        return "admin/skills/skill-form";
    }

    // 신규 스킬 등록 처리
    @PostMapping("/new")
    public String create(@ModelAttribute("skill") BossSkill skill) {
        bossSkillRepository.save(skill);
        return "redirect:/admin/skills";
    }

    // 스킬 수정 폼
    @GetMapping("/{name}/edit")
    public String editForm(@PathVariable String name, Model model) {
        BossSkill skill = bossSkillRepository.findById(name)
                .orElseThrow();
        model.addAttribute("skill", skill);
        model.addAttribute("formAction", "/admin/skills/" + name + "/edit");
        return "admin/skills/skill-form";
    }

    // 스킬 수정 처리
    @PostMapping("/{name}/edit")
    public String update(@PathVariable String name,
                         @ModelAttribute("skill") BossSkill form) {

        BossSkill skill = bossSkillRepository.findById(name)
                .orElseThrow();

        skill.setDescription(form.getDescription());
        skill.setEffectText(form.getEffectText());
        skill.setUseText(form.getUseText());
        // skill.setName(...) 은 그대로 두기

        bossSkillRepository.save(skill);
        return "redirect:/admin/skills";
    }

    // 삭제
    @PostMapping("/{name}/delete")
    public String delete(@PathVariable String name) {
        bossSkillRepository.deleteById(name);
        return "redirect:/admin/skills";
    }
}

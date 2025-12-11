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
        return "admin/skills/skill-list";
    }

    // 신규 스킬 등록 폼
    @GetMapping("/new")
    public String createForm(Model model) {
        BossSkill skill = new BossSkill();
        skill.setMaxCooldown(1); // 기본값: 1턴(쿨 없음)
        model.addAttribute("skill", skill);
        model.addAttribute("formAction", "/admin/skills/new");
        return "admin/skills/skill-form";
    }

    // 신규 스킬 등록 처리
    @PostMapping("/new")
    public String create(@ModelAttribute("skill") BossSkill skill) {
        // 방어 로직: 0 이하로 들어오면 1로 고정
        if (skill.getMaxCooldown() <= 0) {
            skill.setMaxCooldown(1);
        }
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

        // name(PK)는 그대로 두고 나머지만 반영
        skill.setDisplayName(form.getDisplayName());
        skill.setDescription(form.getDescription());
        skill.setEffectText(form.getEffectText());
        skill.setUseText(form.getUseText());
        skill.setMaxCooldown(form.getMaxCooldown() <= 0 ? 1 : form.getMaxCooldown());

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

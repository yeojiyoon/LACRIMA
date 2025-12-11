package com.example.demo.admin;

import com.example.demo.game.Skill;
import com.example.demo.game.SkillForm;
import com.example.demo.game.SkillRepository;
import com.example.demo.game.SkillTag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/user-skills")
public class AdminSkillController {

    private final SkillRepository skillRepository;

    public AdminSkillController(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    // 🔹 목록
    @GetMapping
    public String list(Model model) {
        List<Skill> skills = skillRepository.findAll();
        model.addAttribute("skills", skills);
        return "admin/skill-list";
    }

    // 🔹 신규 등록 폼
    @GetMapping("/new")
    public String newForm(Model model) {
        SkillForm form = new SkillForm();
        model.addAttribute("form", form);
        model.addAttribute("tags", SkillTag.values());

        // 🔥 이 줄이 없어서 editMode == null 이었던 거야
        model.addAttribute("editMode", false);

        return "admin/skill-form";
    }

    // 🔹 신규 등록 처리
    @PostMapping("/new")
    public String create(@ModelAttribute("form") SkillForm form) {
        Skill skill = new Skill(
                form.getCode(),
                form.getName(),
                form.getImageUrl(),
                form.getDescription(),
                form.getEffectText(),
                form.getTag(),
                form.getCooldown() != null ? form.getCooldown() : 0
        );
        skillRepository.save(skill);
        return "redirect:/admin/user-skills";
    }

    // 🔹 수정 폼
    @GetMapping("/{code}/edit")
    public String editForm(@PathVariable String code, Model model) {
        Skill skill = skillRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + code));

        SkillForm form = new SkillForm();
        form.setCode(skill.getCode());
        form.setName(skill.getName());
        form.setImageUrl(skill.getImageUrl());
        form.setDescription(skill.getDescription());
        form.setEffectText(skill.getEffectText());
        form.setTag(skill.getTag());
        form.setCooldown(skill.getCooldown());

        model.addAttribute("form", form);
        model.addAttribute("tags", SkillTag.values());
        model.addAttribute("editMode", true);

        return "admin/skill-form";
    }

    // 🔹 수정 처리
    @PostMapping("/{code}/edit")
    public String update(@PathVariable String code,
                         @ModelAttribute("form") SkillForm form) {

        Skill skill = skillRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + code));

        // code(pk)는 변경 안 한다고 가정
        skill.setName(form.getName());
        skill.setImageUrl(form.getImageUrl());
        skill.setDescription(form.getDescription());
        skill.setEffectText(form.getEffectText());
        skill.setTag(form.getTag());
        skill.setCooldown(form.getCooldown() != null ? form.getCooldown() : 0);

        skillRepository.save(skill);
        return "redirect:/admin/user-skills";
    }

    // 🔹 삭제
    @PostMapping("/{code}/delete")
    public String delete(@PathVariable String code) {
        skillRepository.deleteById(code);
        return "redirect:/admin/user-skills";
    }
}

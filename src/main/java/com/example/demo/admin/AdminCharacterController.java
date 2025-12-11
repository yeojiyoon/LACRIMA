package com.example.demo.admin;

import com.example.demo.auth.AuthService;
import com.example.demo.auth.UnauthorizedException;
import com.example.demo.game.PlayerCharacter;
import com.example.demo.game.PlayerCharacterRepository;
import com.example.demo.game.Skill;
import com.example.demo.game.SkillRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/chars")
public class AdminCharacterController {

    private final AuthService authService;
    private final PlayerCharacterRepository charRepo;
    private final UserAccountRepository userRepo;
    private final SkillRepository skillRepo;  // 🔹 추가

    public AdminCharacterController(AuthService authService,
                                    PlayerCharacterRepository charRepo,
                                    UserAccountRepository userRepo,
                                    SkillRepository skillRepo) { // 🔹 생성자에 추가
        this.authService = authService;
        this.charRepo = charRepo;
        this.userRepo = userRepo;
        this.skillRepo = skillRepo;
    }

    /** 공통: ADMIN 권한 체크 */
    private void requireAdmin(HttpSession session) {
        UserAccount loginUser = authService.requireLogin(session);
        if (!authService.isAdmin(loginUser)) {
            throw new UnauthorizedException("관리자만 접근 가능합니다.");
        }
    }

    /** 캐릭터 목록 */
    @GetMapping
    public String listChars(HttpSession session, Model model) {
        requireAdmin(session);

        model.addAttribute("characters", charRepo.findAll());
        return "admin/chars/list";
    }

    /** 새 캐릭터 생성 폼 */
    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        requireAdmin(session);

        CharacterForm form = new CharacterForm();
        form.setHp(1);          // 기본값
        form.setAtk(1);
        form.setIntelligence(1);
        form.setDet(1);
        form.setActionPoint(1);
        form.setCurrentHp(120); // hp=1 기준 기본값

        model.addAttribute("characterForm", form);
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("skills", skillRepo.findAll());   // 🔹 장착 스킬 선택용
        model.addAttribute("formAction", "/admin/chars/new");
        return "admin/chars/form";
    }

    /** 새 캐릭터 저장 */
    @PostMapping("/new")
    public String createChar(HttpSession session,
                             @ModelAttribute("characterForm") CharacterForm form) {
        requireAdmin(session);

        UserAccount user = userRepo.findById(form.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + form.getUserId()));

        PlayerCharacter pc = new PlayerCharacter(
                user,
                form.getName(),
                form.getAtk(),
                form.getIntelligence(),
                form.getHp(),
                form.getDet()
        );

        pc.setCurrentHp(form.getCurrentHp());
        pc.setActionPoint(form.getActionPoint());

        pc.setPortraitUrl(form.getPortraitUrl());
        pc.setAvatarUrl(form.getAvatarUrl());
        pc.setCatchphrase(form.getCatchphrase());
        pc.setOneLiner1(form.getOneLiner1());
        pc.setOneLiner2(form.getOneLiner2());
        pc.setOneLiner3(form.getOneLiner3());

        if (form.getEquippedSkill1Code() != null && !form.getEquippedSkill1Code().isEmpty()) {
            Skill s1 = skillRepo.findById(form.getEquippedSkill1Code())
                    .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + form.getEquippedSkill1Code()));
            pc.setEquippedSkill1(s1);
        }
        if (form.getEquippedSkill2Code() != null && !form.getEquippedSkill2Code().isEmpty()) {
            Skill s2 = skillRepo.findById(form.getEquippedSkill2Code())
                    .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + form.getEquippedSkill2Code()));
            pc.setEquippedSkill2(s2);
        }

        // 🔹 인벤토리 세팅
        if (form.getInventorySkillCodes() != null) {
            for (String code : form.getInventorySkillCodes()) {
                if (code == null || code.isEmpty()) continue;
                Skill skill = skillRepo.findById(code)
                        .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + code));
                pc.getSkillInventory().add(skill);
            }
        }

        charRepo.save(pc);
        return "redirect:/admin/chars";
    }

    /** 기존 캐릭터 수정 폼 */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               HttpSession session,
                               Model model) {
        requireAdmin(session);

        PlayerCharacter pc = charRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + id));

        CharacterForm form = new CharacterForm();
        form.setId(pc.getId());
        form.setUserId(pc.getUser().getId());
        form.setName(pc.getName());
        form.setAtk(pc.getAtk());
        form.setIntelligence(pc.getIntelligence());
        form.setHp(pc.getHp());
        form.setDet(pc.getDet());
        form.setCurrentHp(pc.getCurrentHp());
        form.setActionPoint(pc.getActionPoint());

        form.setPortraitUrl(pc.getPortraitUrl());
        form.setAvatarUrl(pc.getAvatarUrl());
        form.setCatchphrase(pc.getCatchphrase());
        form.setOneLiner1(pc.getOneLiner1());
        form.setOneLiner2(pc.getOneLiner2());
        form.setOneLiner3(pc.getOneLiner3());

        if (pc.getEquippedSkill1() != null) {
            form.setEquippedSkill1Code(pc.getEquippedSkill1().getCode());
        }
        if (pc.getEquippedSkill2() != null) {
            form.setEquippedSkill2Code(pc.getEquippedSkill2().getCode());
        }

        // 🔹 인벤토리: 엔티티 → 코드 리스트
        form.setInventorySkillCodes(
                pc.getSkillInventory().stream()
                        .map(Skill::getCode)
                        .toList()
        );

        model.addAttribute("characterForm", form);
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("skills", skillRepo.findAll());
        model.addAttribute("formAction", "/admin/chars/" + id + "/edit");
        return "admin/chars/form";
    }



    /** 기존 캐릭터 수정 저장 */
    @PostMapping("/{id}/edit")
    public String updateChar(@PathVariable Long id,
                             HttpSession session,
                             @ModelAttribute("characterForm") CharacterForm form) {
        requireAdmin(session);

        PlayerCharacter pc = charRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + id));

        UserAccount user = userRepo.findById(form.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + form.getUserId()));

        pc.setUser(user);
        pc.setName(form.getName());
        pc.setAtk(form.getAtk());
        pc.setIntelligence(form.getIntelligence());
        pc.setHp(form.getHp());
        pc.setDet(form.getDet());
        pc.setCurrentHp(form.getCurrentHp());
        pc.setActionPoint(form.getActionPoint());

        pc.setPortraitUrl(form.getPortraitUrl());
        pc.setAvatarUrl(form.getAvatarUrl());
        pc.setCatchphrase(form.getCatchphrase());
        pc.setOneLiner1(form.getOneLiner1());
        pc.setOneLiner2(form.getOneLiner2());
        pc.setOneLiner3(form.getOneLiner3());

        // 장착 스킬 1
        if (form.getEquippedSkill1Code() == null || form.getEquippedSkill1Code().isEmpty()) {
            pc.setEquippedSkill1(null);
        } else {
            Skill s1 = skillRepo.findById(form.getEquippedSkill1Code())
                    .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + form.getEquippedSkill1Code()));
            pc.setEquippedSkill1(s1);
        }

        // 장착 스킬 2
        if (form.getEquippedSkill2Code() == null || form.getEquippedSkill2Code().isEmpty()) {
            pc.setEquippedSkill2(null);
        } else {
            Skill s2 = skillRepo.findById(form.getEquippedSkill2Code())
                    .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + form.getEquippedSkill2Code()));
            pc.setEquippedSkill2(s2);
        }

        // 🔹 인벤토리 업데이트 (기존 거 싹 비우고 다시 채우기)
        pc.getSkillInventory().clear();
        if (form.getInventorySkillCodes() != null) {
            for (String code : form.getInventorySkillCodes()) {
                if (code == null || code.isEmpty()) continue;
                Skill skill = skillRepo.findById(code)
                        .orElseThrow(() -> new IllegalArgumentException("Skill not found: " + code));
                pc.getSkillInventory().add(skill);
            }
        }

        charRepo.save(pc);
        return "redirect:/admin/chars";
    }



    /** 캐릭터 삭제 */
    @PostMapping("/{id}/delete")
    public String deleteChar(@PathVariable Long id,
                             HttpSession session) {
        requireAdmin(session);

        charRepo.deleteById(id);
        return "redirect:/admin/chars";
    }
}

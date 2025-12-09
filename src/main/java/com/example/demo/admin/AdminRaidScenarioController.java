package com.example.demo.admin;

import com.example.demo.auth.AuthService;
import com.example.demo.auth.UnauthorizedException;
import com.example.demo.game.BossMonster;
import com.example.demo.game.BossMonsterRepository;
import com.example.demo.game.RaidScenario;
import com.example.demo.game.RaidScenarioRepository;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/raids")
public class AdminRaidScenarioController {

    private final AuthService authService;
    private final RaidScenarioRepository scenarioRepo;
    private final BossMonsterRepository bossRepo;

    public AdminRaidScenarioController(AuthService authService,
                                       RaidScenarioRepository scenarioRepo,
                                       BossMonsterRepository bossRepo) {
        this.authService = authService;
        this.scenarioRepo = scenarioRepo;
        this.bossRepo = bossRepo;
    }

    /** 공통: ADMIN 권한 체크 */
    private void requireAdmin(HttpSession session) {
        UserAccount loginUser = authService.requireLogin(session);
        if (!authService.isAdmin(loginUser)) {
            throw new UnauthorizedException("관리자만 접근 가능합니다.");
        }
    }

    /** 목록 + 생성 폼 */
    @GetMapping
    public String list(HttpSession session, Model model) {
        requireAdmin(session);

        List<RaidScenario> scenarios = scenarioRepo.findAll();
        List<BossMonster> bosses = bossRepo.findAll();

        model.addAttribute("scenarios", scenarios);
        model.addAttribute("bosses", bosses);      // 셀렉트 박스용
        model.addAttribute("form", new RaidScenarioForm());
        return "admin/raid-scenarios";
    }

    /** 새 세트 생성 */
    @PostMapping("/new")
    public String create(HttpSession session,
                         @ModelAttribute("form") RaidScenarioForm form) {

        requireAdmin(session);

        RaidScenario sc = new RaidScenario();
        sc.setName(form.getName());
        sc.setDescription(form.getDescription());

        if (form.getBossId() != null) {
            BossMonster boss = bossRepo.findById(form.getBossId())
                    .orElseThrow(() -> new IllegalArgumentException("보스가 존재하지 않습니다."));
            sc.setBoss(boss);
        }

        scenarioRepo.save(sc);
        return "redirect:/admin/raids";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           HttpSession session,
                           Model model) {

        requireAdmin(session);

        RaidScenario sc = scenarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RaidScenario not found: " + id));

        RaidScenarioForm form = new RaidScenarioForm();
        form.setName(sc.getName());
        form.setDescription(sc.getDescription());

        // 🔹 보스가 있다면 bossId 세팅
        if (sc.getBoss() != null) {
            form.setBossId(sc.getBoss().getId());
            form.setBossName(sc.getBoss().getName());
        }

        model.addAttribute("form", form);
        model.addAttribute("editingId", id);
        model.addAttribute("scenarios", scenarioRepo.findAll());
        model.addAttribute("bosses", bossRepo.findAll());

        return "admin/raid-scenarios";
    }


    /** 기존 세트 수정 저장 */
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         HttpSession session,
                         @ModelAttribute("form") RaidScenarioForm form) {

        requireAdmin(session);

        RaidScenario sc = scenarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RaidScenario not found: " + id));

        sc.setName(form.getName());
        sc.setDescription(form.getDescription());

        if (form.getBossId() != null) {
            BossMonster boss = bossRepo.findById(form.getBossId())
                    .orElseThrow(() -> new IllegalArgumentException("보스가 존재하지 않습니다."));
            sc.setBoss(boss);
        } else {
            sc.setBoss(null);
        }

        scenarioRepo.save(sc);
        return "redirect:/admin/raids";
    }


    /** 세트 삭제 (원하면 사용) */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        scenarioRepo.deleteById(id);
        return "redirect:/admin/raids";
    }

    // === 폼 DTO ===
    public static class RaidScenarioForm {
        private String name;
        private String description;
        private Long bossId;   // 선택한 보스 PK
        private String bossName;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Long getBossId() { return bossId; }
        public void setBossId(Long bossId) { this.bossId = bossId; }

        public String getBossName() { return bossName; }
        public void setBossName(String bossName) { this.bossName = bossName; }
    }
}

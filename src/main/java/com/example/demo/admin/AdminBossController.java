package com.example.demo.admin;

import com.example.demo.auth.AuthService;
import com.example.demo.auth.UnauthorizedException;
import com.example.demo.game.BossMonster;
import com.example.demo.game.BossMonsterRepository;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/bosses")
public class AdminBossController {

    private final AuthService authService;
    private final BossMonsterRepository bossRepo;

    public AdminBossController(AuthService authService,
                               BossMonsterRepository bossRepo) {
        this.authService = authService;
        this.bossRepo = bossRepo;
    }

    /** 공통: ADMIN 권한 체크 */
    private void requireAdmin(HttpSession session) {
        UserAccount loginUser = authService.requireLogin(session);
        if (!authService.isAdmin(loginUser)) {
            throw new UnauthorizedException("관리자만 접근 가능합니다.");
        }
    }

    /** 보스 목록 */
    @GetMapping
    public String listBosses(HttpSession session, Model model) {
        requireAdmin(session);

        model.addAttribute("bosses", bossRepo.findAll());
        return "admin/bosses/list";
    }

    /** 새 보스 생성 폼 */
    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        requireAdmin(session);

        BossForm form = new BossForm();
        form.setMaxHp(1000);
        form.setCurrentHp(1000);
        form.setDefense(10);

        model.addAttribute("bossForm", form);
        model.addAttribute("formAction", "/admin/bosses/new");
        return "admin/bosses/form";
    }

    /** 새 보스 저장 */
    @PostMapping("/new")
    public String createBoss(HttpSession session,
                             @ModelAttribute("bossForm") BossForm form) {
        requireAdmin(session);

        BossMonster boss = new BossMonster(
                form.getName(),
                form.getMaxHp(),
                form.getDefense()
        );
        boss.setCurrentHp(form.getCurrentHp());

        bossRepo.save(boss);
        return "redirect:/admin/bosses";
    }

    /** 기존 보스 수정 폼 */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               HttpSession session,
                               Model model) {
        requireAdmin(session);

        BossMonster boss = bossRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found: " + id));

        BossForm form = new BossForm();
        form.setId(boss.getId());
        form.setName(boss.getName());
        form.setMaxHp(boss.getMaxHp());
        form.setCurrentHp(boss.getCurrentHp());
        form.setDefense(boss.getDefense());

        model.addAttribute("bossForm", form);
        model.addAttribute("formAction", "/admin/bosses/" + id + "/edit");
        return "admin/bosses/form";
    }

    /** 기존 보스 수정 저장 */
    @PostMapping("/{id}/edit")
    public String updateBoss(@PathVariable Long id,
                             HttpSession session,
                             @ModelAttribute("bossForm") BossForm form) {
        requireAdmin(session);

        BossMonster boss = bossRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found: " + id));

        boss.setName(form.getName());
        boss.setMaxHp(form.getMaxHp());
        boss.setCurrentHp(form.getCurrentHp());
        boss.setDefense(form.getDefense());

        bossRepo.save(boss);
        return "redirect:/admin/bosses";
    }

    /** 보스 삭제 */
    @PostMapping("/{id}/delete")
    public String deleteBoss(@PathVariable Long id,
                             HttpSession session) {
        requireAdmin(session);

        bossRepo.deleteById(id);
        return "redirect:/admin/bosses";
    }

    /** (옵션) 보스 HP 리셋: currentHp = maxHp */
    @PostMapping("/{id}/reset-hp")
    public String resetBossHp(@PathVariable Long id,
                              HttpSession session) {
        requireAdmin(session);

        BossMonster boss = bossRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Boss not found: " + id));

        boss.setCurrentHp(boss.getMaxHp());
        bossRepo.save(boss);

        return "redirect:/admin/bosses";
    }
}

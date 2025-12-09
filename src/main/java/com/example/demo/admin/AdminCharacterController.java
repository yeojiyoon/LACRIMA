package com.example.demo.admin;

import com.example.demo.auth.AuthService;
import com.example.demo.auth.UnauthorizedException;
import com.example.demo.game.PlayerCharacter;
import com.example.demo.game.PlayerCharacterRepository;
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

    public AdminCharacterController(AuthService authService,
                                    PlayerCharacterRepository charRepo,
                                    UserAccountRepository userRepo) {
        this.authService = authService;
        this.charRepo = charRepo;
        this.userRepo = userRepo;
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

        // HP 값에 따른 maxHp는 PlayerCharacter 생성자/메서드에서 처리
        PlayerCharacter pc = new PlayerCharacter(
                user,
                form.getName(),
                form.getAtk(),
                form.getIntelligence(),
                form.getHp(),
                form.getDet()
        );
        // currentHp / actionPoint는 필요하면 덮어쓰기
        pc.setCurrentHp(form.getCurrentHp());
        pc.setActionPoint(form.getActionPoint());

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

        model.addAttribute("characterForm", form);
        model.addAttribute("users", userRepo.findAll());
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
        pc.setHp(form.getHp());  // 여기서 maxHp 재계산 + currentHp 조정
        pc.setDet(form.getDet());
        pc.setCurrentHp(form.getCurrentHp());
        pc.setActionPoint(form.getActionPoint());

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

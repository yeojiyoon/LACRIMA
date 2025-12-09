package com.example.demo.admin;

import com.example.demo.auth.AuthService;
import com.example.demo.auth.UnauthorizedException;
import com.example.demo.game.BossMonster;
import com.example.demo.game.BossMonsterRepository;
import com.example.demo.game.PlayerCharacterRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminHomeController {

    private final AuthService authService;
    private final UserAccountRepository userRepo;
    private final PlayerCharacterRepository charRepo;
    private final BossMonsterRepository bossRepo;

    public AdminHomeController(AuthService authService,
                               UserAccountRepository userRepo,
                               PlayerCharacterRepository charRepo,
                               BossMonsterRepository bossRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
        this.charRepo = charRepo;
        this.bossRepo = bossRepo;
    }

    @GetMapping
    public String adminHome(HttpSession session, Model model) {

        // 1) 로그인 안 되어 있으면 예외
        UserAccount loginUser = authService.requireLogin(session);

        // 2) 관리자 권한 아니면 막기
        if (!authService.isAdmin(loginUser)) {
            throw new UnauthorizedException("관리자만 접근 가능합니다.");
        }

        long userCount = userRepo.count();
        long charCount = charRepo.count();
        long bossCount = bossRepo.count();
        List<BossMonster> bosses = bossRepo.findAll();

        model.addAttribute("loginUser", loginUser);
        model.addAttribute("userCount", userCount);
        model.addAttribute("charCount", charCount);
        model.addAttribute("bossCount", bossCount);
        model.addAttribute("bosses", bosses);

        return "admin/home"; // templates/admin/home.html
    }
}

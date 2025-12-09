package com.example.demo.admin;

import com.example.demo.auth.AuthService;
import com.example.demo.game.BossService;
import com.example.demo.game.BossState;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/raid")
public class AdminRaidController {

    private final BossService bossService;
    private final AuthService authService;

    public AdminRaidController(BossService bossService, AuthService authService) {
        this.bossService = bossService;
        this.authService = authService;
    }

    @GetMapping("/init")
    public ResponseEntity<?> initBoss(
            @RequestParam String roomId,
            @RequestParam(defaultValue = "100") int maxHp,
            HttpSession session
    ) {
        UserAccount user = authService.requireLogin(session);
        if (!authService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자만 접근 가능합니다.");
        }

        BossState state = bossService.initBoss(roomId, maxHp);
        return ResponseEntity.ok(state);
    }

    @GetMapping("/state")
    public ResponseEntity<?> getBossState(
            @RequestParam String roomId,
            HttpSession session
    ) {
        UserAccount user = authService.requireLogin(session);
        if (!authService.isAdmin(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("관리자만 접근 가능합니다.");
        }

        BossState state = bossService.getBossState(roomId);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(state);
    }
}

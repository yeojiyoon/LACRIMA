package com.example.demo.web;

import com.example.demo.game.MyInfoSettingService;
import com.example.demo.game.PlayerCharacter;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/my-info/settings")
public class MyInfoSettingController {

    private final MyInfoSettingService service;

    public MyInfoSettingController(MyInfoSettingService service) {
        this.service = service;
    }

    @PostMapping("/one-liners")
    public ResponseEntity<?> updateOneLiners(
            @RequestBody OneLinersUpdateRequest req,
            HttpSession session
    ) {
        // ✅ 세션에서 로그인 유저 꺼내기
        UserAccount loginUser = (UserAccount) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        PlayerCharacter pc = service.updateOneLiners(
                loginUser.getUsername(),
                req.oneLiner1,
                req.oneLiner2,
                req.oneLiner3
        );

        return ResponseEntity.ok(new OneLinersResponse(
                pc.getOneLiner1(),
                pc.getOneLiner2(),
                pc.getOneLiner3()
        ));
    }
}

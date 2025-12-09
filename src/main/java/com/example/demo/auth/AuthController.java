package com.example.demo.auth;

import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserAccountRepository userRepo;

    public AuthController(UserAccountRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        Optional<UserAccount> opt = userRepo.findByUsername(request.getUsername());
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("존재하지 않는 사용자입니다.");
        }

        UserAccount user = opt.get();

        // 🔹 평문 비교 대신 BCrypt 비교
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("비밀번호가 올바르지 않습니다.");
        }

        // 세션에 로그인 정보 저장
        session.setAttribute("loginUser", user);

        LoginResponse res = new LoginResponse(
                user.getUsername(),
                user.getNickname(),
                user.getRole()
        );

        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("로그인되지 않았습니다.");
        }

        LoginResponse res = new LoginResponse(
                user.getUsername(),
                user.getNickname(),
                user.getRole()
        );
        return ResponseEntity.ok(res);
    }
}

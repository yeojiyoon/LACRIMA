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

    public AuthController(UserAccountRepository userRepo) { //init 시점 부팅한 repo 받아옴
        this.userRepo = userRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) { //login.html 에서 session body 전달
        Optional<UserAccount> opt = userRepo.findByUsername(request.getUsername()); //유저 DB 검색(후에 sql 등으로 교체)
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
        session.setMaxInactiveInterval(80 * 60);

        LoginResponse res = new LoginResponse(
                user.getUsername(),
                user.getNickname(),
                user.getRole()
        );

        return ResponseEntity.ok(res); //로그인 승인 리턴
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
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

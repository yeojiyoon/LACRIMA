package com.example.demo.admin;

import com.example.demo.auth.AuthService;
import com.example.demo.auth.PasswordUtil;
import com.example.demo.auth.UnauthorizedException;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AuthService authService;
    private final UserAccountRepository userRepo;

    public AdminUserController(AuthService authService,
                               UserAccountRepository userRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
    }

    /** 로그인 + ADMIN 권한 체크 공통 메서드 */
    private void requireAdmin(HttpSession session) {
        var loginUser = authService.requireLogin(session);
        if (!authService.isAdmin(loginUser)) {
            throw new UnauthorizedException("관리자만 접근 가능합니다.");
        }
    }

    /** 유저 목록 */
    @GetMapping
    public String listUsers(HttpSession session, Model model) {
        requireAdmin(session);

        model.addAttribute("users", userRepo.findAll());
        return "admin/users/list";
    }

    /** 새 유저 생성 폼 */
    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        requireAdmin(session);

        model.addAttribute("userForm", new UserAccount());
        model.addAttribute("formAction", "/admin/users/new");  // ✅ 추가
        return "admin/users/form";
    }

    /** 새 유저 저장 */
    @PostMapping("/new")
    public String createUser(HttpSession session,
                             @ModelAttribute("userForm") UserAccount form) {
        requireAdmin(session);

        // 비밀번호 해시 적용 (PasswordUtil.hash 사용)
        String rawPw = form.getPassword();
        if (rawPw == null || rawPw.isBlank()) {
            // 비밀번호는 필수로 받자 (간단하게)
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        String encodedPw = PasswordUtil.hash(rawPw);
        form.setPassword(encodedPw);

        // role 기본값: USER
        if (form.getRole() == null || form.getRole().isBlank()) {
            form.setRole("USER");
        }

        userRepo.save(form);
        return "redirect:/admin/users";
    }

    /** 기존 유저 수정 폼 */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               HttpSession session,
                               Model model) {
        requireAdmin(session);

        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        model.addAttribute("userForm", user);
        model.addAttribute("formAction", "/admin/users/" + id + "/edit"); // ✅ 추가
        return "admin/users/form";
    }

    /** 기존 유저 수정 저장 */
    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             HttpSession session,
                             @ModelAttribute("userForm") UserAccount form) {
        requireAdmin(session);

        UserAccount user = userRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        user.setUsername(form.getUsername());
        user.setNickname(form.getNickname());
        user.setRole(form.getRole());

        // 비밀번호 입력 칸이 비어있지 않으면 새 비밀번호로 변경
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            String encodedPw = PasswordUtil.hash(form.getPassword());
            user.setPassword(encodedPw);
        }

        userRepo.save(user);
        return "redirect:/admin/users";
    }

    /** 유저 삭제 */
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             HttpSession session) {
        requireAdmin(session);

        userRepo.deleteById(id);
        return "redirect:/admin/users";
    }
}

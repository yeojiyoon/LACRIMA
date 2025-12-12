package com.example.demo.web;

import com.example.demo.game.MemberService;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MemberPageController {

    private final MemberService memberService;

    public MemberPageController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public String membersPage(HttpSession session, Model model) {
        // (선택) 로그인 체크: 네 프로젝트에서 session에 loginUser 넣어두는 흐름이라면 유지
        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("members", memberService.getAllNonAdminMembers());
        return "members"; // templates/members.html
    }
}

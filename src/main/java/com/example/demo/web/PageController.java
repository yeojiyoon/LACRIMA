package com.example.demo.web;

import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login.html";
    }

    @GetMapping("/raid-room")
    public String raidRoom(HttpSession session, Model model) {

        UserAccount user = (UserAccount) session.getAttribute("loginUser");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", user.getUsername());

        // TODO: 캐릭터 정보/파티 정보 넣기
        // model.addAttribute("character", character);
        // model.addAttribute("partyMembers", party);

        return "raid-room";  // templates/raid-room.html 자동 렌더링됨
    }
}

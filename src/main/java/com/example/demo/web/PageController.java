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

    @GetMapping("/my-page")
    public String myPage(HttpSession session, Model model) {
        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("role", user.getRole());

        // TODO: 유저 → 캐릭터 매핑 넣기
        // GameCharacter character = characterService.findByOwner(user.getUsername());
        // model.addAttribute("character", character);

        return "my-page"; // templates/my-page.html
    }

    @GetMapping("/raid-room")
    public String raidRoom(HttpSession session, Model model) {

        UserAccount user = (UserAccount) session.getAttribute("loginUser");

        if (user == null) { //로그인 안되어있을경우
            return "redirect:/login";
        }

        model.addAttribute("username", user.getUsername());

        // TODO: 캐릭터 정보/파티 정보 넣기
        // model.addAttribute("character", character);
        // model.addAttribute("partyMembers", party);

        return "raid-room";  // templates/raid-room.html 자동 렌더링됨
    }
}

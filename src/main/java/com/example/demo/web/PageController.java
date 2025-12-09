package com.example.demo.web;

import com.example.demo.game.PartyMemberView;
import com.example.demo.game.PlayerCharacter;
import com.example.demo.game.PlayerCharacterService;
import com.example.demo.game.RaidPartyService;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {

    private final PlayerCharacterService playerCharacterService;
    private final RaidPartyService raidPartyService;

    public PageController(PlayerCharacterService playerCharacterService,
                          RaidPartyService raidPartyService) {
        this.playerCharacterService = playerCharacterService;
        this.raidPartyService = raidPartyService;
    }

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

        PlayerCharacter character = playerCharacterService.findByUser(user);
        model.addAttribute("character", character);

        return "my-page";
    }

    @GetMapping("/raid-room")
    public String raidRoom(HttpSession session, Model model) {

        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", user.getUsername());

        // 내 캐릭터
        PlayerCharacter me = playerCharacterService.findByUser(user);
        model.addAttribute("character", me);

        // 일단 roomId 하나 고정 (나중에 다중 방 지원 가능)
        String roomId = "raid-1";
        model.addAttribute("roomId", roomId);

        // 레이드방 입장 시 파티에 나를 추가
        raidPartyService.join(roomId, me);

        // 현재 방의 파티 멤버 목록
        List<PartyMemberView> partyMembers = raidPartyService.getPartyMembers(roomId);
        model.addAttribute("partyMembers", partyMembers);

        return "raid-room";
    }
}

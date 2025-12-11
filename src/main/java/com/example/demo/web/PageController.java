package com.example.demo.web;

import com.example.demo.game.*;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PageController {

    private final PlayerCharacterService playerCharacterService;
    private final RaidPartyService raidPartyService;
    private final RaidScenarioRepository raidScenarioRepository;
    private final BossService bossService;

    public PageController(PlayerCharacterService playerCharacterService,
                          RaidPartyService raidPartyService,
                          RaidScenarioRepository raidScenarioRepository,
                          BossService bossService) {
        this.playerCharacterService = playerCharacterService;
        this.raidPartyService = raidPartyService;
        this.raidScenarioRepository = raidScenarioRepository;
        this.bossService = bossService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login.html";
    }

    // ===== 1. 로그인 후 첫 화면: 로비 =====
    @GetMapping("/lobby")
    public String lobby(HttpSession session, Model model) {
        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("role", user.getRole());

        // admin이 만든 레이드 세트 전체
        List<RaidScenario> scenarios = raidScenarioRepository.findByActiveTrue();
        model.addAttribute("scenarios", scenarios);

        return "lobby";
    }

    // 로비에서 세트 선택 → my-page 로 이동
    @PostMapping("/lobby/select")
    public String selectScenario(@RequestParam Long scenarioId) {
        return "redirect:/my-page/" + scenarioId;
    }

    // 기존 /my-page 는 쓰면 로비로 돌려보내기
    @GetMapping("/my-page")
    public String myPageNoScenario() {
        return "redirect:/lobby";
    }

    // ===== 2. 마이페이지 (시나리오 1개와 1:1) =====
    @GetMapping("/my-page/{scenarioId}")
    public String myPage(@PathVariable Long scenarioId,
                         HttpSession session,
                         Model model) {

        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        RaidScenario scenario = raidScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("해당 레이드 세트를 찾을 수 없습니다."));

        model.addAttribute("username", user.getUsername());
        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("role", user.getRole());

        PlayerCharacter character = playerCharacterService.findByUser(user);
        model.addAttribute("character", character);

        model.addAttribute("scenario", scenario);
        model.addAttribute("lobbyRoomId", scenario.getLobbyRoomId());
        model.addAttribute("raidRoomId", scenario.getRaidRoomId());

        return "my-page";
    }

    // ===== 3. 레이드 방 (시나리오 1개와 1:1) =====
    @GetMapping("/raid-room/{scenarioId}")
    public String raidRoom(@PathVariable Long scenarioId,
                           HttpSession session,
                           Model model) {

        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        RaidScenario scenario = raidScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("해당 레이드 세트를 찾을 수 없습니다."));

        // 🔥 공통 유저 정보
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());

        // 🔥 여기! ADMIN 여부 플래그
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("scenario", scenario);

        // 내 캐릭터
        PlayerCharacter me = playerCharacterService.findByUser(user);
        model.addAttribute("character", me);

        // 이 세트에 대응하는 roomId (보스/파티/채팅 모두 이 키로 구분)
        String roomId = scenario.getRaidRoomId();
        model.addAttribute("roomId", roomId);

        // 🔹 DB 보스로 보스 상태 초기화
        BossMonster boss = scenario.getBoss();
        if (boss != null) {
            // BossService의 in-memory BossState를 DB 보스 기준으로 세팅
            bossService.initBoss(roomId, boss);

            // 화면에 표시할 초기값 전달
            long currentHp = boss.getCurrentHp();
            long maxHp = boss.getMaxHp();

            model.addAttribute("bossName", boss.getName());
            model.addAttribute("bossCurrentHp", currentHp);
            model.addAttribute("bossMaxHp", maxHp);
        } else {
            // 보스가 설정되지 않은 세트일 경우 fallback
            model.addAttribute("bossName", "Unknown Boss");
            model.addAttribute("bossCurrentHp", 1000);
            model.addAttribute("bossMaxHp", 1000);
        }

        // 🔥 레이드방 입장 시 파티에 나를 추가
        // → ADMIN 은 관전/조작만 하게 하려면 제외하는 게 자연스러움
        if (!isAdmin && me != null) {
            raidPartyService.join(roomId, me);
        }

        // 현재 방의 파티 멤버 목록
        List<PartyMemberView> partyMembers = raidPartyService.getPartyMembers(roomId);
        model.addAttribute("partyMembers", partyMembers);

        return "raid-room";
    }

    // PageController.java

    @GetMapping("/my-info")
    public String myInfo(HttpSession session, Model model) {
        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("nickname", user.getNickname());
        model.addAttribute("role", user.getRole());

        PlayerCharacter character = playerCharacterService.findByUser(user);
        model.addAttribute("character", character);

        // TODO: 나중에 실제 스킬 리스트 넣기
        // model.addAttribute("skills", skillService.findByCharacter(character));

        return "my-info";
    }
}

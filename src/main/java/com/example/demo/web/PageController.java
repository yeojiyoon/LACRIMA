package com.example.demo.web;

import com.example.demo.game.*;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.demo.game.Skill;
import com.example.demo.game.SkillRepository;


import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class PageController {

    private final PlayerCharacterService playerCharacterService;
    private final RaidPartyService raidPartyService;
    private final RaidScenarioRepository raidScenarioRepository;
    private final BossService bossService;
    private final PlayerCharacterRepository playerCharacterRepository;
    private final SkillRepository skillRepository;   // 🔹 추가

    public PageController(PlayerCharacterService playerCharacterService,
                          RaidPartyService raidPartyService,
                          RaidScenarioRepository raidScenarioRepository,
                          BossService bossService, PlayerCharacterRepository playerCharacterRepository, SkillRepository skillRepository) {
        this.playerCharacterService = playerCharacterService;
        this.raidPartyService = raidPartyService;
        this.raidScenarioRepository = raidScenarioRepository;
        this.bossService = bossService;
        this.playerCharacterRepository = playerCharacterRepository;
        this.skillRepository = skillRepository;
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

        // 🔹 내 캐릭터 조회
        PlayerCharacter myChar = playerCharacterRepository.findByUser(user)
                .orElse(null);

        // 🔹 유저 정보는 객체 그대로 싣기 (필요하면 템플릿에서 loginUser.username 등으로 접근)
        model.addAttribute("loginUser", user);

        // 🔹 캐릭터도 통째로
        model.addAttribute("myCharacter", myChar);

        // 기존처럼 직접 값도 싣고 싶으면 유지해도 됨 (점진적 마이그레이션용)
        model.addAttribute("username", user.getUsername()); //리펙터링할때 수정.
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

        PlayerCharacter myChar = playerCharacterRepository.findByUser(user)
                .orElse(null);

        // 여러 이름으로 다 실어보내기 (호환용)
        model.addAttribute("loginUser", user);
        model.addAttribute("username", user.getUsername()); // 옛 템플릿 호환
        model.addAttribute("character", myChar);            // 옛 템플릿 호환
        model.addAttribute("myCharacter", myChar);          // 새 코드

        return "my-info";
    }


    @PostMapping("/my-info/skill/equip")
    @ResponseBody
    public ResponseEntity<?> equipSkill(@RequestBody Map<String, String> body,
                                        HttpSession session) {

        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).build();

        PlayerCharacter ch = playerCharacterRepository.findByUser(user)
                .orElseThrow();

        String skillCode = body.get("skillCode");
        Skill skill = skillRepository.findById(skillCode).orElseThrow();

        // 이미 장착했는지 체크
        if (!skill.equals(ch.getEquippedSkill1()) &&
                !skill.equals(ch.getEquippedSkill2())) {

            if (ch.getEquippedSkill1() == null) {
                ch.setEquippedSkill1(skill);
            } else if (ch.getEquippedSkill2() == null) {
                ch.setEquippedSkill2(skill);
            } else {
                return ResponseEntity.badRequest().body("모든 슬롯이 가득 찼습니다.");
            }

            playerCharacterRepository.save(ch);
        }

        // 🔥 여기서부터 Map.of 대신 HashMap 사용
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("slot1", SkillDTO.from(ch.getEquippedSkill1()));  // null 가능
        resp.put("slot2", SkillDTO.from(ch.getEquippedSkill2()));  // null 가능

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/my-info/skill/unequip")
    @ResponseBody
    public ResponseEntity<?> unequipSkill(@RequestBody Map<String, String> body,
                                          HttpSession session) {

        UserAccount user = (UserAccount) session.getAttribute("loginUser");
        if (user == null) return ResponseEntity.status(401).build();

        PlayerCharacter ch = playerCharacterRepository.findByUser(user)
                .orElseThrow();

        int slot = Integer.parseInt(body.get("slot"));

        if (slot == 1) ch.setEquippedSkill1(null);
        else if (slot == 2) ch.setEquippedSkill2(null);

        playerCharacterRepository.save(ch);

        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("slot1", SkillDTO.from(ch.getEquippedSkill1()));  // null OK
        resp.put("slot2", SkillDTO.from(ch.getEquippedSkill2()));  // null OK

        return ResponseEntity.ok(resp);
    }

}

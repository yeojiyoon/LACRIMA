package com.example.demo.websocket;

import com.example.demo.game.*;
import com.example.demo.user.UserAccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHandler extends TextWebSocketHandler {

    private static final int MAX_USERS = 20;

    // 전체 접속 세션 (인원 제한용)
    private final Set<WebSocketSession> allSessions = ConcurrentHashMap.newKeySet();

    // roomId -> 그 방에 있는 세션들
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    // 세션 -> 이 세션이 속한 roomId
    private final Map<WebSocketSession, String> sessionRoom = new ConcurrentHashMap<>();

    // 세션 -> 이 세션의 캐릭터 ID (파티에서 제거할 때 사용)
    private final Map<WebSocketSession, Long> sessionCharacter = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RaidGameService raidGameService;
    private final PlayerCharacterService playerCharacterService;
    private final RaidPartyService raidPartyService;
    private final UserAccountRepository userAccountRepository;

    // 🔐 username 기준 ADMIN 체크
    private boolean isAdmin(String username) {
        return userAccountRepository.findByUsername(username)
                .map(user -> "ADMIN".equalsIgnoreCase(user.getRole()))
                .orElse(false);
    }

    public ChatHandler(RaidGameService raidGameService,
                       PlayerCharacterService playerCharacterService,
                       RaidPartyService raidPartyService,
                       UserAccountRepository userAccountRepository) {
        this.raidGameService = raidGameService;
        this.playerCharacterService = playerCharacterService;
        this.raidPartyService = raidPartyService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (allSessions.size() >= MAX_USERS) {
            session.sendMessage(new TextMessage("방이 가득 찼습니다."));
            session.close(CloseStatus.POLICY_VIOLATION);
            System.out.println("접속 거부(인원 초과)");
            return;
        }

        allSessions.add(session);
        String username = getUsername(session);
        System.out.println("새 연결: " + username);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String username = getUsername(session);
        String payload = message.getPayload().trim();
        System.out.println("raw payload: " + payload);

        ChatMessage chatMessage;

        try {
            chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        } catch (Exception e) {
            chatMessage = new ChatMessage();
            chatMessage.setType(MessageType.CHAT);
            chatMessage.setSender(username);
            chatMessage.setMessage(payload);
        }

        if (chatMessage.getSender() == null || chatMessage.getSender().isBlank()) {
            chatMessage.setSender(username);
        }
        if (chatMessage.getType() == null) {
            chatMessage.setType(MessageType.CHAT);
        }

        switch (chatMessage.getType()) {
            case JOIN:
                handleJoin(session, chatMessage);
                break;
            case LEAVE:
                handleLeave(session, chatMessage);
                break;
            case ATTACK:
                handleAttack(session, chatMessage);
                break;
            case DEFEND:
                handleDefend(session, chatMessage);
                break;
            case ADMIN:
                handleAdmin(session, chatMessage);
                break;
            case CHAT:
            default:
                handleChat(session, chatMessage);
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = getUsername(session);
        allSessions.remove(session);

        String roomId = sessionRoom.remove(session);
        if (roomId != null) {
            Set<WebSocketSession> roomSessions = rooms.get(roomId);
            if (roomSessions != null) {
                roomSessions.remove(session);
                if (roomSessions.isEmpty()) {
                    rooms.remove(roomId);
                    System.out.println("방 삭제: " + roomId);
                }
            }

            Long charId = sessionCharacter.remove(session);
            if (charId != null) {
                raidPartyService.leave(roomId, charId);
            }

            ChatMessage systemMsg = new ChatMessage();
            systemMsg.setType(MessageType.SYSTEM);
            systemMsg.setSender("SYSTEM");
            systemMsg.setRoomId(roomId);
            systemMsg.setMessage(username + "님이 퇴장했습니다. 현재 인원: " +
                    (roomSessions != null ? roomSessions.size() : 0));

            broadcastToRoom(roomId, systemMsg);

            sendPartyUpdate(roomId);
        }

        System.out.println("퇴장: " + username + ", status=" + status);
    }

    // ================== 타입별 처리 ==================

    // 방 입장
    private void handleJoin(WebSocketSession session, ChatMessage msg) throws Exception {
        String username = msg.getSender();
        String roomId = msg.getRoomId();

        if (roomId == null || roomId.isBlank()) {
            roomId = "lobby";
            msg.setRoomId(roomId);
        }

        Set<WebSocketSession> roomSessions =
                rooms.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet());

        roomSessions.add(session);
        sessionRoom.put(session, roomId);

        boolean admin = isAdmin(username);

        // ADMIN은 파티에 안 넣고, 일반 유저만 파티에 추가
        if (!admin) {
            PlayerCharacter pc = playerCharacterService.findByUsername(username);
            if (pc != null) {
                sessionCharacter.put(session, pc.getId());
                raidPartyService.join(roomId, pc);
            }
        }

        int partyCount = raidPartyService.getPartyMembers(roomId).size();

        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setType(MessageType.SYSTEM);
        systemMsg.setSender("SYSTEM");
        systemMsg.setRoomId(roomId);

        if (admin) {
            systemMsg.setMessage("관리자 " + username + "이(가) 방에 입장했습니다. (파티: " + partyCount + ")");
        } else {
            systemMsg.setMessage(username + "님이 방에 입장했습니다. (파티: " + partyCount + ")");
        }

        broadcastToRoom(roomId, systemMsg);
        sendPartyUpdate(roomId);
    }

    // 일반 채팅
    private void handleChat(WebSocketSession session, ChatMessage msg) throws Exception {
        String roomId = resolveRoomId(session, msg);
        if (roomId == null) {
            ChatMessage warn = new ChatMessage();
            warn.setType(MessageType.SYSTEM);
            warn.setSender("SYSTEM");
            warn.setMessage("먼저 방에 입장(JOIN)해야 채팅할 수 있습니다.");
            sendToSession(session, warn);
            return;
        }

        msg.setRoomId(roomId);
        broadcastToRoom(roomId, msg);
    }

    // 공격
    private void handleAttack(WebSocketSession session, ChatMessage msg) throws Exception {
        String roomId = resolveRoomId(session, msg);
        if (roomId == null) {
            ChatMessage warn = new ChatMessage();
            warn.setType(MessageType.SYSTEM);
            warn.setSender("SYSTEM");
            warn.setMessage("먼저 방에 입장(JOIN)해야 공격할 수 있습니다.");
            sendToSession(session, warn);
            return;
        }

        String username = msg.getSender();
        if (username == null || username.isBlank()) {
            username = getUsername(session);
        }

        String comment = msg.getComment();

        try {
            PlayerCharacter pc = playerCharacterService.findByUsername(username);

            if (pc != null && pc.getActionPoint() <= 0) {
                ChatMessage warn = new ChatMessage();
                warn.setType(MessageType.SYSTEM);
                warn.setSender("SYSTEM");
                warn.setRoomId(roomId);
                warn.setMessage(pc.getName() + "는 이미 이번 턴에 행동했습니다.");
                sendToSession(session, warn);
                return;
            }

            AttackResult result = raidGameService.handleAttack(roomId, username, pc);

            // 1) 플레이어 공격 결과 먼저 전송
            ChatMessage resultMsg = new ChatMessage();
            resultMsg.setType(MessageType.ATTACK_RESULT);
            resultMsg.setSender(username);
            resultMsg.setRoomId(roomId);
            resultMsg.setMessage(result.getMessage());
            resultMsg.setDamage(result.getDamage());
            resultMsg.setBossHp(result.getBossHp());
            resultMsg.setMaxHp(result.getMaxHp());
            resultMsg.setComment(comment);
            resultMsg.setTurn(result.getTurn());

            broadcastToRoom(roomId, resultMsg);

            // 2) 보스 사망이면 여기서 바로 처리하고 종료
            if (result.isBossDead()) {
                ChatMessage deadMsg = new ChatMessage();
                deadMsg.setType(MessageType.BOSS_DEAD);
                deadMsg.setRoomId(roomId);
                deadMsg.setSender("SYSTEM");
                deadMsg.setMessage(username + "가(이) 보스를 처치했습니다!");

                broadcastToRoom(roomId, deadMsg);
                return;
            }

            // 3) 보스 스킬 로그가 있으면 약간 딜레이 후 SYSTEM 메시지로 전송
            String bossSkillText = result.getBossSkillText();
            if (bossSkillText != null && !bossSkillText.isBlank()) {
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                ChatMessage skillMsg = new ChatMessage();
                skillMsg.setType(MessageType.SYSTEM); // 별도 타입 만들기 귀찮으니 SYSTEM 사용
                skillMsg.setSender("SYSTEM");
                skillMsg.setRoomId(roomId);
                skillMsg.setMessage(bossSkillText);

                broadcastToRoom(roomId, skillMsg);
            }

            // 4) 턴 종료 시에만 보스 공격/턴 처리
            if (result.isTurnEnded()) {
                var bossHits = result.getBossHits();
                if (bossHits != null && !bossHits.isEmpty()) {

                    // 보스 스킬 메시지 이후, 또 살짝 딜레이
                    try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                    for (RaidGameService.BossHit hit : bossHits) {
                        ChatMessage bossMsg = new ChatMessage();
                        bossMsg.setType(MessageType.BOSS_ATTACK);
                        bossMsg.setSender("BOSS");
                        bossMsg.setRoomId(roomId);

                        bossMsg.setTargetName(hit.getName());
                        bossMsg.setDamage(hit.getDamage());
                        bossMsg.setTargetHp(hit.getHpAfter());
                        bossMsg.setTargetMaxHp(hit.getMaxHp());
                        bossMsg.setDefense(hit.getDefense());

                        // 🔥 이 공격은 "이번 턴"에 일어남
                        bossMsg.setTurn(result.getTurn());

                        broadcastToRoom(roomId, bossMsg);
                    }
                }

                // 파티 HP 갱신
                sendPartyUpdate(roomId);

                // 🔥 파티 전멸이면 GAME_OVER만 보내고 종료
                if (result.isPartyWiped()) {
                    ChatMessage loseMsg = new ChatMessage();
                    loseMsg.setType(MessageType.GAME_OVER);
                    loseMsg.setRoomId(roomId);
                    loseMsg.setSender("SYSTEM");
                    loseMsg.setMessage("모든 캐릭터의 HP가 0이 되어 전투에서 패배했습니다.");

                    broadcastToRoom(roomId, loseMsg);
                    return;
                }

                // 🔥 여기서 "다음 턴 시작" 알림을 별도로 보냄
                int nextTurn = raidGameService.getTurn(roomId);

// 🔥 Executor 기반 쿨타임 스냅샷 가져오기
                BossSkillExecutor.SkillCooldownInfo cdInfo =
                        raidGameService.getSkillCooldownInfo(roomId);

                ChatMessage turnMsg = new ChatMessage();
                turnMsg.setType(MessageType.TURN_START);
                turnMsg.setRoomId(roomId);
                turnMsg.setTurn(nextTurn);
                turnMsg.setMessage("보스가 다시 당신들을 주시한다.");

// 스킬1
                turnMsg.setSkill1CdNow(cdInfo.getSkill1CdNow());
                turnMsg.setSkill1CdMax(cdInfo.getSkill1CdMax());
                turnMsg.setSkill1Available(cdInfo.isSkill1Available());
                turnMsg.setSkill1Name(cdInfo.getSkill1Name());
                turnMsg.setSkill1Desc(cdInfo.getSkill1Desc());

// 스킬2
                turnMsg.setSkill2CdNow(cdInfo.getSkill2CdNow());
                turnMsg.setSkill2CdMax(cdInfo.getSkill2CdMax());
                turnMsg.setSkill2Available(cdInfo.isSkill2Available());
                turnMsg.setSkill2Name(cdInfo.getSkill2Name());
                turnMsg.setSkill2Desc(cdInfo.getSkill2Desc());

// 스킬3
                turnMsg.setSkill3CdNow(cdInfo.getSkill3CdNow());
                turnMsg.setSkill3CdMax(cdInfo.getSkill3CdMax());
                turnMsg.setSkill3Available(cdInfo.isSkill3Available());
                turnMsg.setSkill3Name(cdInfo.getSkill3Name());
                turnMsg.setSkill3Desc(cdInfo.getSkill3Desc());

                broadcastToRoom(roomId, turnMsg);

            }
        } catch (Exception e) {
            e.printStackTrace();

            ChatMessage errorMsg = new ChatMessage();
            errorMsg.setType(MessageType.SYSTEM);
            errorMsg.setSender("SYSTEM");
            errorMsg.setRoomId(roomId);
            errorMsg.setMessage("공격 처리 중 오류가 발생했습니다: " + e.getMessage());

            broadcastToRoom(roomId, errorMsg);
        }
    }

    // 방어
    private void handleDefend(WebSocketSession session, ChatMessage msg) throws Exception {
        String roomId = resolveRoomId(session, msg);
        if (roomId == null) {
            ChatMessage warn = new ChatMessage();
            warn.setType(MessageType.SYSTEM);
            warn.setSender("SYSTEM");
            warn.setMessage("먼저 방에 입장(JOIN)해야 방어할 수 있습니다.");
            sendToSession(session, warn);
            return;
        }

        String username = msg.getSender();
        if (username == null || username.isBlank()) {
            username = getUsername(session);
        }

        Long targetCharId = msg.getTargetCharacterId();
        if (targetCharId == null) {
            ChatMessage warn = new ChatMessage();
            warn.setType(MessageType.SYSTEM);
            warn.setSender("SYSTEM");
            warn.setRoomId(roomId);
            warn.setMessage("방어 대상을 선택해야 합니다.");
            sendToSession(session, warn);
            return;
        }

        PlayerCharacter defender = playerCharacterService.findByUsername(username);
        if (defender == null) {
            ChatMessage warn = new ChatMessage();
            warn.setType(MessageType.SYSTEM);
            warn.setSender("SYSTEM");
            warn.setRoomId(roomId);
            warn.setMessage("캐릭터 정보가 없어 방어할 수 없습니다.");
            sendToSession(session, warn);
            return;
        }

        AttackResult result =
                raidGameService.handleDefend(roomId, defender, targetCharId, msg.getComment());

        // 1) 방어 결과 먼저 전송
        ChatMessage resultMsg = new ChatMessage();
        resultMsg.setType(MessageType.DEFEND_RESULT);
        resultMsg.setSender(username);
        resultMsg.setRoomId(roomId);
        resultMsg.setMessage(result.getMessage());
        resultMsg.setDamage(result.getDamage());
        resultMsg.setBossHp(result.getBossHp());
        resultMsg.setMaxHp(result.getMaxHp());
        resultMsg.setComment(msg.getComment());
        resultMsg.setTurn(result.getTurn());

        broadcastToRoom(roomId, resultMsg);

        // 2) 보스 스킬 로그가 있으면 약간 딜레이 후 SYSTEM 메시지로 전송
        String bossSkillText = result.getBossSkillText();
        if (bossSkillText != null && !bossSkillText.isBlank()) {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            ChatMessage skillMsg = new ChatMessage();
            skillMsg.setType(MessageType.SYSTEM);
            skillMsg.setSender("SYSTEM");
            skillMsg.setRoomId(roomId);
            skillMsg.setMessage(bossSkillText);

            broadcastToRoom(roomId, skillMsg);
        }

        if (result.isTurnEnded()) {

            var bossHits = result.getBossHits();
            if (bossHits != null && !bossHits.isEmpty()) {
                // 스킬 로그 이후 약간 딜레이
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

                for (RaidGameService.BossHit hit : bossHits) {
                    ChatMessage bossMsg = new ChatMessage();
                    bossMsg.setType(MessageType.BOSS_ATTACK);
                    bossMsg.setSender("BOSS");
                    bossMsg.setRoomId(roomId);

                    bossMsg.setTargetName(hit.getName());
                    bossMsg.setDamage(hit.getDamage());
                    bossMsg.setTargetHp(hit.getHpAfter());
                    bossMsg.setTargetMaxHp(hit.getMaxHp());
                    bossMsg.setDefense(hit.getDefense());

                    bossMsg.setTurn(result.getTurn()); // 🔥 현재 턴

                    broadcastToRoom(roomId, bossMsg);
                }
            }

            sendPartyUpdate(roomId);

            // 🔥 파티 전멸 체크
            if (result.isPartyWiped()) {
                ChatMessage loseMsg = new ChatMessage();
                loseMsg.setType(MessageType.GAME_OVER);
                loseMsg.setRoomId(roomId);
                loseMsg.setSender("SYSTEM");
                loseMsg.setMessage("모든 캐릭터의 HP가 0이 되어 전투에서 패배했습니다.");

                broadcastToRoom(roomId, loseMsg);
                return;
            }

            int nextTurn = raidGameService.getTurn(roomId);
            // 🔥 여기 추가: 쿨타임 스냅샷 가져오기
            BossSkillExecutor.SkillCooldownInfo cdInfo =
                    raidGameService.getSkillCooldownInfo(roomId);

            ChatMessage turnMsg = new ChatMessage();
            turnMsg.setType(MessageType.TURN_START);
            turnMsg.setRoomId(roomId);
            turnMsg.setTurn(nextTurn);
            turnMsg.setMessage("보스가 다시 당신들을 주시한다.");

            // 🔥 스킬1
            turnMsg.setSkill1CdNow(cdInfo.getSkill1CdNow());
            turnMsg.setSkill1CdMax(cdInfo.getSkill1CdMax());
            turnMsg.setSkill1Available(cdInfo.isSkill1Available());
            turnMsg.setSkill1Name(cdInfo.getSkill1Name());
            turnMsg.setSkill1Desc(cdInfo.getSkill1Desc());

            // 🔥 스킬2
            turnMsg.setSkill2CdNow(cdInfo.getSkill2CdNow());
            turnMsg.setSkill2CdMax(cdInfo.getSkill2CdMax());
            turnMsg.setSkill2Available(cdInfo.isSkill2Available());
            turnMsg.setSkill2Name(cdInfo.getSkill2Name());
            turnMsg.setSkill2Desc(cdInfo.getSkill2Desc());

            // 🔥 스킬3
            turnMsg.setSkill3CdNow(cdInfo.getSkill3CdNow());
            turnMsg.setSkill3CdMax(cdInfo.getSkill3CdMax());
            turnMsg.setSkill3Available(cdInfo.isSkill3Available());
            turnMsg.setSkill3Name(cdInfo.getSkill3Name());
            turnMsg.setSkill3Desc(cdInfo.getSkill3Desc());

            broadcastToRoom(roomId, turnMsg);
        }
    }

    // LEAVE
    private void handleLeave(WebSocketSession session, ChatMessage msg) throws Exception {
        String username = msg.getSender();
        String roomId = sessionRoom.get(session);
        if (roomId == null) {
            return;
        }

        Set<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            roomSessions.remove(session);
            if (roomSessions.isEmpty()) {
                rooms.remove(roomId);
                System.out.println("방 삭제: " + roomId);
            }
        }
        sessionRoom.remove(session);

        Long charId = sessionCharacter.remove(session);
        if (charId != null) {
            raidPartyService.leave(roomId, charId);
        }

        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setType(MessageType.SYSTEM);
        systemMsg.setSender("SYSTEM");
        systemMsg.setRoomId(roomId);
        int partyCount = raidPartyService.getPartyMembers(roomId).size();
        systemMsg.setMessage(username + "님이 방에서 나갔습니다. 현재 인원: " +
                (roomSessions != null ? partyCount : 0));

        broadcastToRoom(roomId, systemMsg);
        sendPartyUpdate(roomId);
    }

    // ================== ADMIN 처리 ==================

    private void handleAdmin(WebSocketSession session, ChatMessage msg) throws Exception {
        String username = msg.getSender();
        if (username == null || username.isBlank()) {
            username = getUsername(session);
        }

        // 🔐 DB role 기반 체크
        if (!isAdmin(username)) {
            ChatMessage warn = new ChatMessage();
            warn.setType(MessageType.SYSTEM);
            warn.setSender("SYSTEM");
            warn.setMessage("관리자 권한이 없습니다.");
            sendToSession(session, warn);
            return;
        }

        String roomId = resolveRoomId(session, msg);
        if (roomId == null || roomId.isBlank()) {
            roomId = msg.getRoomId();
        }
        if (roomId == null || roomId.isBlank()) {
            roomId = "raid-1"; // 기본값
        }

        String command = msg.getCommand();

        if ("START_BATTLE".equals(command)) {
            raidGameService.startBattle(roomId);

            // 🔥 시작할 때도 쿨타임 스냅샷
            BossSkillExecutor.SkillCooldownInfo cdInfo =
                    raidGameService.getSkillCooldownInfo(roomId);

            ChatMessage turnMsg = new ChatMessage();
            turnMsg.setType(MessageType.TURN_START);
            turnMsg.setRoomId(roomId);
            turnMsg.setTurn(1);
            turnMsg.setMessage("전투가 시작되었습니다! 보스가 당신들을 주시한다.");

            turnMsg.setSkill1CdNow(cdInfo.getSkill1CdNow());
            turnMsg.setSkill1CdMax(cdInfo.getSkill1CdMax());
            turnMsg.setSkill1Available(cdInfo.isSkill1Available());
            turnMsg.setSkill1Name(cdInfo.getSkill1Name());
            turnMsg.setSkill1Desc(cdInfo.getSkill1Desc());

            turnMsg.setSkill2CdNow(cdInfo.getSkill2CdNow());
            turnMsg.setSkill2CdMax(cdInfo.getSkill2CdMax());
            turnMsg.setSkill2Available(cdInfo.isSkill2Available());
            turnMsg.setSkill2Name(cdInfo.getSkill2Name());
            turnMsg.setSkill2Desc(cdInfo.getSkill2Desc());

            turnMsg.setSkill3CdNow(cdInfo.getSkill3CdNow());
            turnMsg.setSkill3CdMax(cdInfo.getSkill3CdMax());
            turnMsg.setSkill3Available(cdInfo.isSkill3Available());
            turnMsg.setSkill3Name(cdInfo.getSkill3Name());
            turnMsg.setSkill3Desc(cdInfo.getSkill3Desc());

            broadcastToRoom(roomId, turnMsg);
        }

        // TODO: FORCE_NEXT_TURN 등 추가 커맨드 나중에 더 넣기
    }

    // ================== 유틸 ==================

    private String resolveRoomId(WebSocketSession session, ChatMessage msg) {
        String roomId = msg.getRoomId();
        if (roomId != null && !roomId.isBlank()) {
            return roomId;
        }
        return sessionRoom.get(session);
    }

    private void broadcastToRoom(String roomId, ChatMessage msg) throws Exception {
        Set<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions == null || roomSessions.isEmpty()) {
            return;
        }

        String json = objectMapper.writeValueAsString(msg);
        TextMessage textMessage = new TextMessage(json);

        for (WebSocketSession s : roomSessions) {
            if (!s.isOpen()) continue;
            try {
                s.sendMessage(textMessage);
            } catch (Exception e) {
                System.out.println("⚠ 메시지 전송 실패, 세션 제거: " + s.getId());
                try { s.close(); } catch (Exception ignored) {}
            }
        }
    }

    private void sendToSession(WebSocketSession session, ChatMessage msg) throws Exception {
        String json = objectMapper.writeValueAsString(msg);
        session.sendMessage(new TextMessage(json));
    }

    private String getUsername(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            return principal.getName();
        }
        return session.getId();
    }

    private void sendPartyUpdate(String roomId) throws Exception {
        var partyList = raidPartyService.getPartyMembers(roomId);

        ChatMessage partyMsg = new ChatMessage();
        partyMsg.setType(MessageType.PARTY_UPDATE);
        partyMsg.setRoomId(roomId);
        partyMsg.setParty(partyList);

        broadcastToRoom(roomId, partyMsg);
    }
}

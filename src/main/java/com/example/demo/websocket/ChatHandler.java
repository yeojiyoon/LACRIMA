package com.example.demo.websocket;

import com.example.demo.game.*;
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

    public ChatHandler(RaidGameService raidGameService,
                       PlayerCharacterService playerCharacterService,
                       RaidPartyService raidPartyService) {
        this.raidGameService = raidGameService;
        this.playerCharacterService = playerCharacterService;
        this.raidPartyService = raidPartyService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 아직 방에 넣지는 않고, 전체 접속자에만 추가
        if (allSessions.size() >= MAX_USERS) {
            session.sendMessage(new TextMessage("방이 가득 찼습니다."));
            session.close(CloseStatus.POLICY_VIOLATION);
            System.out.println("접속 거부(인원 초과)");
            return;
        }

        allSessions.add(session);
        String username = getUsername(session);
        System.out.println("새 연결: " + username);
        // 실질적인 "입장" 알림은 JOIN 메시지를 받았을 때 처리
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
            // JSON 아니면 비상용: 그냥 채팅으로 처리
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

            // 🔹 파티에서 캐릭터 제거
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

            // 🔹 파티 정보 갱신
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
            roomId = "lobby"; // 기본 방 이름
            msg.setRoomId(roomId);
        }

        // 방 세션 set 가져오기 (없으면 새로 만듦)
        Set<WebSocketSession> roomSessions =
                rooms.computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet());

        roomSessions.add(session);
        sessionRoom.put(session, roomId);

        System.out.println("JOIN: " + username + " -> " + roomId +
                " (인원: " + roomSessions.size() + ")");

        // 🔹 sender 이름 기준으로 PlayerCharacter 찾기
        PlayerCharacter pc = playerCharacterService.findByUsername(username);
        if (pc != null) {
            sessionCharacter.put(session, pc.getId()); // 세션 → 캐릭터 ID 저장
            raidPartyService.join(roomId, pc); // 파티에 등록
        }

        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setType(MessageType.SYSTEM);
        systemMsg.setSender("SYSTEM");
        systemMsg.setRoomId(roomId);
        systemMsg.setMessage(username + "님이 " + roomId +
                " 방에 입장했습니다. 현재 인원: " + roomSessions.size());

        broadcastToRoom(roomId, systemMsg);

        // 🔹 파티 정보 전체 브로드캐스트
        sendPartyUpdate(roomId);
    }

    // 일반 채팅
    private void handleChat(WebSocketSession session, ChatMessage msg) throws Exception {
        String roomId = resolveRoomId(session, msg);
        if (roomId == null) {
            // 방에 속해있지 않으면 안내만 보내고 무시
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
            // 방에 안 들어와 있으면 경고
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

        try {
            // 🔹 username 기준으로 캐릭터 찾아오기
            PlayerCharacter pc = playerCharacterService.findByUsername(username);

            // 🔹 이제 보스/레이드 관련 로직은 전부 RaidGameService로 위임
            AttackResult result = raidGameService.handleAttack(roomId, username, pc);

            ChatMessage resultMsg = new ChatMessage();
            resultMsg.setType(MessageType.ATTACK_RESULT);
            resultMsg.setSender("SYSTEM");
            resultMsg.setRoomId(roomId);
            resultMsg.setMessage(result.getMessage());

            if (result.getBossHp() != null) {
                resultMsg.setBossHp(result.getBossHp());
            }
            if (result.getMaxHp() != null) {
                resultMsg.setMaxHp(result.getMaxHp());
            }

            broadcastToRoom(roomId, resultMsg);

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


    // 사용자가 LEAVE 타입을 직접 보냈을 때 (선택)
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

        // 🔹 파티에서 제거
        Long charId = sessionCharacter.remove(session);
        if (charId != null) {
            raidPartyService.leave(roomId, charId);
        }

        ChatMessage systemMsg = new ChatMessage();
        systemMsg.setType(MessageType.SYSTEM);
        systemMsg.setSender("SYSTEM");
        systemMsg.setRoomId(roomId);
        systemMsg.setMessage(username + "님이 방에서 나갔습니다. 현재 인원: " +
                (roomSessions != null ? roomSessions.size() : 0));

        broadcastToRoom(roomId, systemMsg);

        // 🔹 파티 정보 갱신
        sendPartyUpdate(roomId);
    }

    // ================== 유틸 ==================

    // 메시지/세션에서 roomId 결정
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

    // 🔹 파티 정보 전체를 PARTY_UPDATE로 브로드캐스트
    private void sendPartyUpdate(String roomId) throws Exception {
        var partyList = raidPartyService.getPartyMembers(roomId);

        ChatMessage partyMsg = new ChatMessage();
        partyMsg.setType(MessageType.PARTY_UPDATE);
        partyMsg.setRoomId(roomId);
        partyMsg.setParty(partyList);

        broadcastToRoom(roomId, partyMsg);
    }
}

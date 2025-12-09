package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RaidPartyService {

    // roomId -> 캐릭터 ID 목록
    private final Map<String, List<Long>> roomMembers = new ConcurrentHashMap<>();

    private final PlayerCharacterService playerCharacterService;

    public RaidPartyService(PlayerCharacterService playerCharacterService) {
        this.playerCharacterService = playerCharacterService;
    }

    /**
     * 파티에 캐릭터 참가
     */
    public synchronized void join(String roomId, PlayerCharacter pc) {
        List<Long> members = roomMembers.computeIfAbsent(roomId, id -> new ArrayList<>());
        Long charId = pc.getId();
        if (!members.contains(charId)) {
            members.add(charId);
        }
    }

    /**
     * 파티에서 캐릭터 제거
     */
    public synchronized void leave(String roomId, Long characterId) {
        List<Long> members = roomMembers.get(roomId);
        if (members == null) return;

        members.remove(characterId);
        if (members.isEmpty()) {
            roomMembers.remove(roomId);
        }
    }

    /**
     * 현재 파티원 목록 (항상 "최신 HP"로 PartyMemberView를 새로 만들어서 반환)
     */
    public List<PartyMemberView> getPartyMembers(String roomId) {
        List<Long> members = roomMembers.get(roomId);
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        List<PartyMemberView> result = new ArrayList<>();
        for (Long charId : members) {
            PlayerCharacter pc = playerCharacterService.findById(charId);
            if (pc == null) continue;
            result.add(PartyMemberView.from(pc)); // 🔥 현재 DB 상태 기준으로 새로 생성
        }
        return result;
    }
}

package com.example.demo.game;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RaidPartyService {

    // roomId → (characterId → PlayerCharacter)
    private final Map<String, Map<Long, PlayerCharacter>> parties = new ConcurrentHashMap<>();

    // 레이드 방에 캐릭터 참가
    public void join(String roomId, PlayerCharacter character) {
        if (character == null) return;

        parties.computeIfAbsent(roomId, id -> new ConcurrentHashMap<>())
                .put(character.getId(), character);
    }

    // 레이드 방에서 캐릭터 퇴장
    public void leave(String roomId, Long characterId) {
        Map<Long, PlayerCharacter> party = parties.get(roomId);
        if (party == null) return;

        party.remove(characterId);

        if (party.isEmpty()) {
            parties.remove(roomId);
        }
    }

    // 현재 방의 파티 멤버 목록 (뷰용 DTO로 변환)
    public List<PartyMemberView> getPartyMembers(String roomId) {
        Map<Long, PlayerCharacter> party = parties.get(roomId);
        if (party == null) {
            return Collections.emptyList();
        }

        List<PartyMemberView> result = new ArrayList<>();
        for (PlayerCharacter pc : party.values()) {
            result.add(PartyMemberView.from(pc));
        }
        return result;
    }
}

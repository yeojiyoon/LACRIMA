package com.example.demo.game;

import com.example.demo.user.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class PlayerCharacterService {

    private final PlayerCharacterRepository playerCharacterRepository;

    public PlayerCharacterService(PlayerCharacterRepository playerCharacterRepository) {
        this.playerCharacterRepository = playerCharacterRepository;
    }

    // UserAccount로 캐릭터 찾기
    public PlayerCharacter findByUser(UserAccount user) {
        return playerCharacterRepository.findByUser(user)
                .orElse(null); // 없으면 null → 템플릿에서 '미등록' 처리
    }

    // username 문자열로 찾고 싶으면 이렇게도 가능
    public PlayerCharacter findByUsername(String username) {
        return playerCharacterRepository.findByUserUsername(username)
                .orElse(null);
    }
}

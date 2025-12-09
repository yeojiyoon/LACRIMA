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
                .orElse(null);
    }

    // username 문자열로 찾기
    public PlayerCharacter findByUsername(String username) {
        return playerCharacterRepository.findByUserUsername(username)
                .orElse(null);
    }

    // 🔹 id로 조회
    public PlayerCharacter findById(Long id) {
        return playerCharacterRepository.findById(id)
                .orElse(null);
    }

    // 🔹 저장
    public PlayerCharacter save(PlayerCharacter pc) {
        return playerCharacterRepository.save(pc);
    }
}

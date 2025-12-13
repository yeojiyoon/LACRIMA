package com.example.demo.game;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyInfoSettingService {

    private final PlayerCharacterRepository repo;

    public MyInfoSettingService(PlayerCharacterRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public PlayerCharacter updateOneLiners(
            String username,
            String one1,
            String one2,
            String one3
    ) {
        PlayerCharacter pc = repo.findByUserUsername(username)
                .orElseThrow(() -> new IllegalStateException("캐릭터가 없습니다."));

        pc.setOneLiner1(clean(one1));
        pc.setOneLiner2(clean(one2));
        pc.setOneLiner3(clean(one3));

        return pc;
    }

    private String clean(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        return s.length() > 255 ? s.substring(0, 255) : s;
    }
}

package com.example.demo.game;

import com.example.demo.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerCharacterRepository extends JpaRepository<PlayerCharacter, Long> {

    // UserAccount 기준으로 캐릭터 찾기 (1:1 가정)
    Optional<PlayerCharacter> findByUser(UserAccount user);

    // username 문자열로도 찾고 싶으면 이 메서드도 만들어둘 수 있음
    Optional<PlayerCharacter> findByUserUsername(String username);
}

package com.example.demo.game;

import com.example.demo.user.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerCharacterRepository extends JpaRepository<PlayerCharacter, Long> {

    Optional<PlayerCharacter> findByUserId(Long userId);
    // UserAccount 기준으로 캐릭터 찾기 (1:1 가정)
    Optional<PlayerCharacter> findByUser(UserAccount user);

    // username 문자열로도 찾고 싶으면 이 메서드도 만들어둘 수 있음
    Optional<PlayerCharacter> findByUserUsername(String username);

    @EntityGraph(attributePaths = {"skillInventory"})
    @Query("""
    select pc
    from PlayerCharacter pc
    join pc.user u
    where upper(u.role) not like '%ADMIN%'
    order by pc.id asc
""")
    List<PlayerCharacter> findAllNonAdminWithSkills();
}

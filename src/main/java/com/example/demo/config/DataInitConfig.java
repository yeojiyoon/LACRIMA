package com.example.demo.config;

import com.example.demo.auth.PasswordUtil;
import com.example.demo.game.BossMonster;
import com.example.demo.game.BossMonsterRepository;
import com.example.demo.game.PlayerCharacter;
import com.example.demo.game.PlayerCharacterRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitConfig {

    @Bean
    public CommandLineRunner initAll(UserAccountRepository userRepo,
                                     BossMonsterRepository bossRepo,
                                     PlayerCharacterRepository charRepo) {
        return args -> {

            // 1) 유저 먼저
            if (userRepo.count() == 0) {
                UserAccount admin = new UserAccount(
                        "admin",
                        PasswordUtil.hash("admin1234"),
                        "ADMIN",
                        "관리자"
                );
                UserAccount alice = new UserAccount(
                        "alice",
                        PasswordUtil.hash("alice123"),
                        "USER",
                        "앨리스"
                );
                UserAccount bob = new UserAccount(
                        "bob",
                        PasswordUtil.hash("bob123"),
                        "USER",
                        "밥"
                );

                userRepo.save(admin);
                userRepo.save(alice);
                userRepo.save(bob);

                System.out.println("✅ 기본 유저 3명 생성(BCrypt): admin / alice / bob");
            }

            // 2) 보스
            if (bossRepo.count() == 0) {
                BossMonster boss = new BossMonster(
                        "카오스 드래곤",
                        1000,
                        30
                );
                bossRepo.save(boss);
                System.out.println("🐉 보스 생성: " + boss.getName());
            }

            // 3) 캐릭터 (유저가 존재한다고 가정하고 만들기)
            userRepo.findByUsername("alice").ifPresent(user -> {
                charRepo.findByUser(user).orElseGet(() -> {
                    PlayerCharacter c = new PlayerCharacter(
                            user,
                            "소드 앨리스",
                            5,
                            5,
                            1,
                            5
                    );
                    System.out.println("🎮 캐릭터 생성: " + c.getName());
                    return charRepo.save(c);
                });
            });

            userRepo.findByUsername("bob").ifPresent(user -> {
                charRepo.findByUser(user).orElseGet(() -> {
                    PlayerCharacter c = new PlayerCharacter(
                            user,
                            "마법사 밥",
                            3,
                            4,
                            5,
                            3
                    );
                    System.out.println("🎮 캐릭터 생성: " + c.getName());
                    return charRepo.save(c);
                });
            });
        };
    }
}

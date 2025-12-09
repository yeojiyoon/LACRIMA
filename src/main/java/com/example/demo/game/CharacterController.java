package com.example.demo.game;

import com.example.demo.auth.AuthService;
import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/character")
public class CharacterController {

    private final PlayerCharacterRepository charRepo;
    private final AuthService authService;

    public CharacterController(PlayerCharacterRepository charRepo, AuthService authService) {
        this.charRepo = charRepo;
        this.authService = authService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> myCharacter(HttpSession session) {
        UserAccount user = authService.requireLogin(session);

        return charRepo.findByUser(user)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("해당 유저의 캐릭터가 없습니다."));
    }
}

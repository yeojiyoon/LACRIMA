package com.example.demo.config;

//설정파일 (서버)
//웹소켓 설정 (엔드포인트 등)

import com.example.demo.game.BossService;
import com.example.demo.game.PlayerCharacterService;
import com.example.demo.game.RaidPartyService;
import com.example.demo.websocket.ChatHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class webSocketConfig implements WebSocketConfigurer {

    private final BossService bossService;
    private final PlayerCharacterService playerCharacterService;
    private final RaidPartyService raidPartyService;

    public webSocketConfig(BossService bossService,
                           PlayerCharacterService playerCharacterService,
                           RaidPartyService raidPartyService) {
        this.bossService = bossService;
        this.playerCharacterService = playerCharacterService;
        this.raidPartyService = raidPartyService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/ws/chat")
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler chatHandler() {
        return new ChatHandler(
                bossService,
                playerCharacterService,
                raidPartyService
        );
    }
}

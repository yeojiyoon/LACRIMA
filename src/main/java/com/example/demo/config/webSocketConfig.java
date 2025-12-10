package com.example.demo.config;

import com.example.demo.game.PlayerCharacterService;
import com.example.demo.game.RaidGameService;
import com.example.demo.game.RaidPartyService;
import com.example.demo.user.UserAccountRepository;
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

    private final RaidGameService raidGameService;
    private final PlayerCharacterService playerCharacterService;
    private final RaidPartyService raidPartyService;
    private final UserAccountRepository userAccountRepository;

    public webSocketConfig(RaidGameService raidGameService,
                           PlayerCharacterService playerCharacterService,
                           RaidPartyService raidPartyService,
                           UserAccountRepository userAccountRepository) {
        this.raidGameService = raidGameService;
        this.playerCharacterService = playerCharacterService;
        this.raidPartyService = raidPartyService;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/ws/chat")
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler chatHandler() {
        return new ChatHandler(
                raidGameService,
                playerCharacterService,
                raidPartyService,
                userAccountRepository
        );
    }
}

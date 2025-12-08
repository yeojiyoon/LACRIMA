package com.example.demo.config;

//설정파일 (서버)
//웹소켓 설정 (엔드포인트 등)

import com.example.demo.websocket.EchoWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class webSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(echoHandler(),"/ws/echo")
                //CORS 허용 도메인
                .setAllowedOriginPatterns("*");
        // 아마도 접속을 허용하는 도메인으로 보임. (클라이언트,서버)
    }

    @Bean
    public WebSocketHandler echoHandler() {
        return new EchoWebSocketHandler();
    }
}

package com.example.demo.websocket;

//메시지를 처리하는 핸들러 클래스 (서버)

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class EchoWebSocketHandler extends TextWebSocketHandler {
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("✅ WebSocket 연결 열림: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload(); //클라이언트가 보낸 문자열
        System.out.println("메시지" + payload);

        String response = "echo " + payload;
        session.sendMessage(new TextMessage(response));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        System.out.println("❌ WebSocket 연결 종료: " + session.getId() + " / status=" + status);
    }
}

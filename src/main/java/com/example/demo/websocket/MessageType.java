package com.example.demo.websocket;

public enum MessageType {
    // 클라이언트 → 서버
    CHAT,       // 일반 채팅
    JOIN,       // 방 입장
    LEAVE,      // 방 퇴장
    ATTACK,     // 공격 (보스 레이드용)
    DEFEND,
    PARTY_UPDATE,
    BOSS_ATTACK,

    // 서버 → 클라이언트
    ATTACK_RESULT, // 공격 결과
    DEFEND_RESULT,
    BOSS_UPDATE,   // 보스 체력/상태 갱신
    BOSS_DEAD,     // 보스 사망
    TURN_START,
    GAME_OVER,

    SYSTEM,    // 시스템 메시지 (입장/퇴장 안내 등)
    ADMIN      // 관리자용 메시지 (원하면 나중에 활용)
}

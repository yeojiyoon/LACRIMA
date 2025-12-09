package com.example.demo.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    private static final int COST = 12; // 해시 강도 (10~12 정도면 충분)

    // 비밀번호 해싱
    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
        }
        return BCrypt.withDefaults()
                .hashToString(COST, rawPassword.toCharArray());
    }

    // 비밀번호 검증
    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        BCrypt.Result result = BCrypt.verifyer()
                .verify(rawPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}

package com.example.demo.auth;

public class LoginResponse {
    private String username;
    private String nickname;
    private String role;

    public LoginResponse() {}

    public LoginResponse(String username, String nickname, String role) {
        this.username = username;
        this.nickname = nickname;
        this.role = role;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }
}

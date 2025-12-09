package com.example.demo.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;   // 암호화된 비밀번호

    @Column(nullable = false)
    private String role;       // 예: ROLE_USER, ROLE_ADMIN

    @Column(nullable = true)
    private String nickname;   // 나중에 채팅에 사용할 닉네임

    public UserAccount() {
    }

    public UserAccount(String username, String password, String role, String nickname) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.nickname = nickname;
    }

    // getter/setter (롬복 안 쓰면 자동 생성)
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}

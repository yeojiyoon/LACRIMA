package com.example.demo.auth;

import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String SESSION_KEY = "loginUser";

    public UserAccount getLoginUser(HttpSession session) {
        Object obj = session.getAttribute(SESSION_KEY);
        if (obj instanceof UserAccount user) {
            return user;
        }
        return null;
    }

    public UserAccount requireLogin(HttpSession session) {
        UserAccount user = getLoginUser(session);
        if (user == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }
        return user;
    }

    public boolean isAdmin(UserAccount user) {
        return user != null && "ADMIN".equals(user.getRole());
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}

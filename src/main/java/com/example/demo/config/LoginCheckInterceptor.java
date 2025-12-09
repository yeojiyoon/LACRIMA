package com.example.demo.config;

import com.example.demo.user.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String uri = request.getRequestURI();

        // ✅ H2 콘솔은 로그인 체크 없이 통과
        if (uri.startsWith("/h2-console")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        UserAccount user = (session != null)
                ? (UserAccount) session.getAttribute("loginUser")
                : null;

        boolean isLoginPage =
                uri.equals("/login") ||
                        uri.equals("/login.html") ||
                        uri.equals("/api/login");

        // 1) 로그인 안 된 상태
        if (user == null) {
            // 로그인 관련 주소는 그냥 통과
            if (isLoginPage) {
                return true;
            }

            // 그 외는 로그인 페이지로
            response.sendRedirect("/login");
            return false;
        }

        // 2) 로그인 된 상태
        if (user != null && isLoginPage) { //이거 나중에 응용해서 입장된 상태면 전투방으로만 들어가게
            // 이미 로그인 했는데 /login 가면 /my-page로 튕기기
            response.sendRedirect("/my-page");
            return false;
        }

        // 그 외는 정상 진행
        return true;
    }
}

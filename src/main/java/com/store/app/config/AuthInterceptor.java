package com.store.app.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String path = request.getRequestURI();

        // Allow auth endpoints
        if (path.startsWith("/api/auth")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("USER") == null) {
            response.setStatus(401);
            return false;
        }
        return true;
    }
}

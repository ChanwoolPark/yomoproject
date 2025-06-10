package com.project.yomozomo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String uri = request.getRequestURI();

        // 💥 로그인 페이지 자체로 오면 그냥 보여주기만 하고 리다이렉트 하지 말기
        if (uri.equals("/login") || uri.startsWith("/login/form")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // redirect 말고 상태만 줌
            return;
        }

        response.sendRedirect("/login?needAuth=true&redirect=" + uri);
    }

}

package com.project.yomozomo.security;

import com.project.yomozomo.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        System.out.println("▶▶▶ OAuth2LoginSuccessHandler 호출됨");

        OAuth2User oauthUser = ((OAuth2AuthenticationToken) authentication).getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (!userService.existsByEmail(email)) {
            // 신규 가입 흐름
            response.sendRedirect("/signup");
        } else {
            response.sendRedirect("/");
        }
    }
}

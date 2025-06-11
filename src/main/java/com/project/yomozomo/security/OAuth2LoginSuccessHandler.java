package com.project.yomozomo.security;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public OAuth2LoginSuccessHandler(@Lazy UserService userService) {
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

        boolean isNew = !userService.existsByEmail(email);

        if (isNew) {
            // 👇 세션에 OAuth2 유저 정보 저장
            request.getSession().setAttribute("oauthUser", oauthUser);
            response.sendRedirect("/signup");
        } else {
            Optional<User> userOpt = userService.getUserByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // 1. 탈퇴 상태 복구(30일 이내라면)
                if ("Y".equals(user.getIsWithdrawn())) {
                    userService.recoverWithdrawn(user);
                }

                // 2. 로그인 날짜 갱신
                userService.updateLastLoginDate(user);

                // 3. 휴면 상태 체크 → 휴면이면 안내페이지
                if ("Y".equals(user.getIsDormant())) {
                    response.sendRedirect("/dormant-info");
                    return;
                }
            }

            response.sendRedirect("/");
        }
    }


}

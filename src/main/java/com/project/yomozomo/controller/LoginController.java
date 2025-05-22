package com.project.yomozomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String loginSelect() {
        return "login"; // 소셜/일반 선택 페이지
    }

    @GetMapping("/login/form")
    public String loginForm() {
        return "login-form"; // 일반 로그인 폼
    }
    // POST 방식은 Spring Security가 자동 처리
}


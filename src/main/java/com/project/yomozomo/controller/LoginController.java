package com.project.yomozomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login-page")
    public String loginSelect() {
        return "login"; // resources/templates/login.html
    }
    @GetMapping("/login/form")
    public String loginForm() {
        return "login-form";
    }
}

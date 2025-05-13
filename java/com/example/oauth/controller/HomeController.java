package com.example.oauth.controller;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {        
        if (principal != null) {          
            model.addAttribute("user", Map.of(
                    "name", principal.getAttribute("name"),
                    "email", principal.getAttribute("email"),
                    "picture", principal.getAttribute("picture")
            ));
            //그냥 현재 상황을 t,f로 여부로 저장하는거뿐
            model.addAttribute("authenticated", true);
        } else {
            //인증되지 않은 경우
            model.addAttribute("authenticated", false);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
package com.project.yomozomo.controller;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 메인 페이지 (템플릿 엔진으로 렌더링)
    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal User user) {
        // 필요한 데이터 모델에 담기...
        // model.addAttribute("user", user);
        // ...
        return "index";       // → src/main/resources/templates/index.html
    }


    @GetMapping("/category")
    public String category() {
        return "redirect:/category.html"; // 해당 html이 static에 존재할 경우
    }

    @GetMapping("/login")
    public String login() {
        return "login.html";
    }

    @GetMapping("/logout")
    public String logout() {
        // 세션 만료 등 처리
        return "redirect:/";
    }

    @GetMapping("/my")
    public String myPage() {
        return "redirect:/my.html";
    }

    @GetMapping("/chatbot")
    public String chatbot() {
        return "redirect:/chatbot.html";
    }

    @GetMapping("/support")
    public String support() {
        return "redirect:/support.html";
    }

    @GetMapping("/explore")
    public String explore() {
        return "redirect:/explore.html";
    }

    @GetMapping("/ads")
    public String ads() {
        return "redirect:/ads.html";
    }


}

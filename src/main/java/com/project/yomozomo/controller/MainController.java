package com.project.yomozomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String showMainPage(Model model) {
        model.addAttribute("message", "메인 페이지에 오신 것을 환영합니다!");
        return "main";  // main.html 렌더링
    }
}


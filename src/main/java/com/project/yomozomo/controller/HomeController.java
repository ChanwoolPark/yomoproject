package com.project.yomozomo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal User user) {
        return "index";
    }




    @GetMapping("/category")
    public String category() {
        return "redirect:/category.html"; // 해당 html이 static에 존재할 경우
    }

    @GetMapping("/logout")
    public String logout() {
        // 세션 만료 등 처리
        return "redirect:/";
    }

/*    @GetMapping("/my")
    public String myPage() {
        return "redirect:/my.html";
    }*/

//    @GetMapping("/chatbot")
//    public String chatbot() {
//        return "redirect:/chatbot.html";
//    }

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

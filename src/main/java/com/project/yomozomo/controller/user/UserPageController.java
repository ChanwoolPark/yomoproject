package com.project.yomozomo.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserPageController {
    @GetMapping("/find-id")
    public String showFindIdPage() {
        return "find-id";
    }
    @GetMapping("/find-password")
    public String showFindPasswordPage() {
        return "find-password";
    }
}

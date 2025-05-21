package com.project.yomozomo.controller;

import com.project.yomozomo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserCheckController {
    private final UserService userService;

    @GetMapping("/api/user/check-username")
    public boolean checkUsername(@RequestParam String username) {
        // 존재하면 true(중복), 없으면 false(사용 가능)
        return userService.existsByUsername(username);
    }

    @GetMapping("/api/user/check-email")
    public boolean checkEmail(@RequestParam String email) {
        return userService.existsByEmail(email);
    }

    @GetMapping("/api/user/check-phone")
    public boolean checkPhone(@RequestParam String phone) {
        return userService.existsByPhone(phone);
    }
}


package com.project.yomozomo.controller;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class MyPageController {

    private final UserRepository userRepository;

    @GetMapping("/my")
    public String myPage(Model model, Principal principal) {
        // 로그인된 유저 정보 가져오기
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        model.addAttribute("user", user);
        return "my/myPage"; // 템플릿 위치
    }

    @GetMapping("/my/profile")
    public String profileView(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        return "my/profile"; // fragment로만 쓸 수도 있음
    }



}

package com.project.yomozomo.controller;

import com.project.yomozomo.dto.SignupForm;
import com.project.yomozomo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SignupController {
    private final UserService userService;

    @Value("${juso.confmKey}")
    private String jusoKey;

    // OAuth2 로그인 성공 시, 신규 유저면 /signup 으로 redirect
    @GetMapping("/signup")
    public String showSignupForm(Model model,
                                 @AuthenticationPrincipal OAuth2User oauth2User,
                                 @RequestParam(required = false) String error) {

        SignupForm form = new SignupForm();
        if (oauth2User != null) {
            // 구글/카카오/네이버가 제공한 기본 정보 추출
            form.setEmail(oauth2User.getAttribute("email"));
            form.setName(oauth2User.getAttribute("name"));
            form.setProfileImageUrl(oauth2User.getAttribute("picture"));
        }
        model.addAttribute("signupForm", form);
        model.addAttribute("jusoKey", jusoKey);
        return "signup";  // templates/signup.html
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute SignupForm signupForm, RedirectAttributes rt) {
        if (userService.existsByEmail(signupForm.getEmail())) {
            rt.addFlashAttribute("error", "이미 가입된 이메일입니다.");
            return "redirect:/signup?error";
        }
        userService.registerNewUser(signupForm);
        return "redirect:/login";
    }

    @GetMapping("/juso-popup")
    public String jusoPopup(Model model) {
        // jusoKey 가 이미 필드로 있고 application.yml 에서 주입되므로
        model.addAttribute("jusoKey", jusoKey);
        return "jusoPopup";  // resources/templates/jusoPopup.html
    }

}

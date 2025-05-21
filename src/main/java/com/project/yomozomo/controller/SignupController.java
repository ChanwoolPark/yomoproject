package com.project.yomozomo.controller;

import com.project.yomozomo.dto.SignupForm;
import com.project.yomozomo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SignupController {
    private final UserService userService;

    // OAuth2 로그인 성공 시, 신규 유저면 /signup 으로 redirect
    @GetMapping("/signup")
    public String showSignupForm(Model model,
                                 @AuthenticationPrincipal OAuth2User oauth2User,
                                 @RequestParam(required = false) String error) {

        SignupForm form = new SignupForm();
        boolean oauthSignup = false;
        if (oauth2User != null) {
            // 구글/카카오/네이버가 제공한 기본 정보 추출
            form.setEmail(oauth2User.getAttribute("email"));
            form.setName(oauth2User.getAttribute("name"));
            form.setProfileImageUrl(oauth2User.getAttribute("picture"));
            oauthSignup = true;
        }
        model.addAttribute("signupForm", form);
        model.addAttribute("oauthSignup", oauthSignup);
        return "signup";  // templates/signup.html
    }

    @PostMapping("/signup")
    public String processSignup(@ModelAttribute SignupForm signupForm,
                                @RequestParam(value="profileImage", required=false) MultipartFile profileImage,
                                RedirectAttributes rt) throws IOException {
        MultipartFile profileImageFile = signupForm.getProfileImageFile();

        String filename = null;
        if (!profileImage.isEmpty()) {
            filename = UUID.randomUUID() + "_" + profileImage.getOriginalFilename();
            File dest = new File("C:/YomoProject/uploads/" + filename);
            profileImage.transferTo(dest);
            signupForm.setProfileImageUrl("/uploads/" + filename); // DB에는 URL 저장!
        }

        if (userService.existsByEmail(signupForm.getEmail())) {
            rt.addFlashAttribute("error", "이미 가입된 이메일입니다.");
            return "redirect:/signup?error";
        }

        // 로그인 처리까지 하고 싶으면 userService에서 처리하게 하세요!
        userService.registerNewUser(signupForm);

        // ✅ 회원가입 후 메인으로!
        return "redirect:/";
    }




}

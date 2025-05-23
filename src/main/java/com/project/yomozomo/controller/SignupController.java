package com.project.yomozomo.controller;

import com.project.yomozomo.dto.SignupForm;
import com.project.yomozomo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping("/signup")
    public String showSignupForm(Model model,
                                 HttpServletRequest request,
                                 @RequestParam(required = false) String error) {

        SignupForm form = new SignupForm();
        boolean oauthSignup = false;

        OAuth2User oauth2User = (OAuth2User) request.getSession().getAttribute("oauthUser");

        if (oauth2User != null) {
            oauthSignup = true;

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name"); // 네이버/구글용
            Object nickname = oauth2User.getAttribute("nickname"); // 카카오용
            Object profileImg = oauth2User.getAttribute("profile_image_url");
            Object picture = oauth2User.getAttribute("picture");

            // ✅ 이메일이 없으면 fallback 설정
            if (email == null || email.isBlank()) {
                // 카카오는 고유 식별자로 name을 줌 (String)
                String fallbackId = oauth2User.getName(); // 고유 유저 ID
                email = "kakao_" + fallbackId;
            }
            form.setEmail(email);

            // ✅ 이름 세팅 (nickname을 name으로 사용)
            if (nickname != null) {
                form.setName((String) nickname); // 카카오용
            } else if (name != null) {
                form.setName(name); // 구글/네이버용
            }

            // ✅ 프로필 이미지 설정
            if (profileImg != null) {
                form.setProfileImageUrl((String) profileImg);
            } else if (picture != null) {
                form.setProfileImageUrl((String) picture);
            }

            // ✅ 세션 정보 1회성 사용
            request.getSession().removeAttribute("oauthUser");
        }

        model.addAttribute("signupForm", form);
        model.addAttribute("oauthSignup", oauthSignup);
        return "signup";
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

        // ✅ 이메일이 중복되면 에러
        if (signupForm.getEmail() != null && userService.existsByEmail(signupForm.getEmail())) {
            rt.addFlashAttribute("error", "이미 가입된 이메일입니다.");
            return "redirect:/signup?error";
        }

        // 로그인 처리까지 하고 싶으면 userService에서 처리하게 하세요!
        userService.registerNewUser(signupForm);

        // ✅ 회원가입 후 메인으로!
        return "redirect:/";
    }




}

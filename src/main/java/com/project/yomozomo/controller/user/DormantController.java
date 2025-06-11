package com.project.yomozomo.controller.user;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.MailService;
import com.project.yomozomo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class DormantController {
    private final UserService userService;
    private final MailService mailService;

    // 휴면 안내 페이지
    @GetMapping("/dormant-info")
    public String dormantInfo(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "user/dormant-info"; // thymeleaf 템플릿 이름 (user/dormant-info.html)
    }

    @PostMapping("/dormant/send-auth")
    public String sendDormantAuthMail(@RequestParam String email, Model model) {
        // 1. 인증코드 생성 및 메일 발송 (MailService 사용)
        String code = mailService.sendDormantAuthMail(email);
        // 2. 인증코드 세션 or DB에 저장 (간단히 세션 사용 예시)
        model.addAttribute("email", email);
        return "user/dormant-auth";
    }

    @PostMapping("/dormant/verify-auth")
    public String verifyDormantAuth(@RequestParam String email, @RequestParam String code, Model model) {
        // 1. 코드 일치 여부 확인 (세션/DB에서 꺼내 비교)
        if (mailService.verifyDormantCode(email, code)) {
            // 2. 휴면 해제
            User user = userService.getUserByEmail(email).orElseThrow();
            userService.clearDormant(user);
            // 3. 안내 페이지로
            return "user/dormant-cleared";
        }
        // 인증 실패
        model.addAttribute("error", "인증코드가 일치하지 않습니다.");
        model.addAttribute("email", email);
        return "user/dormant-auth";
    }


}

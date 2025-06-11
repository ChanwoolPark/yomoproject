package com.project.yomozomo.controller.user;

import com.project.yomozomo.service.MailService;
import com.project.yomozomo.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserCheckController {
    private final UserService userService;
    private final MailService mailService;

    @GetMapping("/check-username")
    public boolean checkUsername(@RequestParam String username) {
        return userService.existsByUsername(username);
    }
    @GetMapping("/check-email")
    public boolean checkEmail(@RequestParam String email) {
        return userService.existsByEmail(email);
    }
    @GetMapping("/check-phone")
    public boolean checkPhone(@RequestParam String phone) {
        return userService.existsByPhone(phone);
    }
    @PostMapping("/find-id")
    public String findId(@RequestParam String name, @RequestParam String email) {
        return userService.findUsernameByNameAndEmail(name, email)
                .orElse("일치하는 회원 정보가 없습니다.");
    }
    @GetMapping("/check-nickname")
    @ResponseBody
    public boolean checkNickname(@RequestParam String nickname) {
        return userService.existsByNickname(nickname);
    }

    // 1. 인증번호 발송 (POST)
    @PostMapping("/send-verify-code")
    public String sendVerifyCode(@RequestParam String username, @RequestParam String email, HttpSession session) {
        // 1. 아이디, 이메일 매칭 체크
        if (!userService.existsByUsernameAndEmail(username, email)) {
            return "아이디와 이메일이 일치하지 않습니다.";
        }
        // 2. 인증번호 생성 및 메일 발송
        String code = String.valueOf((int)((Math.random()*900000)+100000)); // 6자리 숫자
        mailService.sendVerificationCode(email, code);
        session.setAttribute("verifyCode", code);         // 인증번호 세션에 저장
        session.setAttribute("verifyTarget", username);    // 혹시라도 보안용
        return "인증번호가 메일로 발송되었습니다.";
    }

    // 2. 인증번호 검증 (POST)
    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String code, HttpSession session) {
        String real = (String) session.getAttribute("verifyCode");
        if (real != null && real.equals(code)) {
            session.setAttribute("codeVerified", true);
            return "인증 성공";
        } else {
            return "인증번호가 일치하지 않습니다.";
        }
    }

    // 3. 비밀번호 재설정 (POST)
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username,
                                @RequestParam String newPassword,
                                HttpSession session) {
        // 비밀번호 유효성 검사
        String pwRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/\\\\|`~]).{8,20}$";
        if (!newPassword.matches(pwRegex)) {
            return "비밀번호는 영어, 숫자, 특수문자를 포함하여 8~20자여야 합니다.";
        }
        Boolean verified = (Boolean) session.getAttribute("codeVerified");
        String verifyTarget = (String) session.getAttribute("verifyTarget");
        if (verified != null && verified && verifyTarget.equals(username)) {
            userService.updatePassword(username, newPassword);
            // 세션 정보 지우기
            session.removeAttribute("verifyCode");
            session.removeAttribute("codeVerified");
            session.removeAttribute("verifyTarget");
            return "<span>비밀번호가 변경되었습니다. </span><a href=\"/login\" style=\"text-decoration:underline;\">로그인하러가기</a>";
        } else {
            return "이메일 인증을 먼저 완료해주세요.";
        }
    }

}


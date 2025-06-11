package com.project.yomozomo.controller.user;

import com.project.yomozomo.dto.UserEditForm;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class UserInfoController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 비밀번호 변경 페이지 (조각)
    @GetMapping("/password-change")
    public String passwordChangePage() {
        return "mypage/fragments/edit-password";
    }

    @PostMapping("/password-change")
    @ResponseBody
    public Map<String, Object> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String currentPw,
            @RequestParam String newPw
    ) {
        Map<String, Object> result = new HashMap<>();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        // 현재 비밀번호 불일치
        if (!passwordEncoder.matches(currentPw, user.getPassword())) {
            result.put("success", false);
            result.put("message", "현재 비밀번호가 올바르지 않습니다.");
            return result;
        }
        // 새 비번이 현재와 동일
        if (passwordEncoder.matches(newPw, user.getPassword())) {
            result.put("success", false);
            result.put("message", "이전 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
            return result;
        }

        // 비밀번호 변경!
        user.setPassword(passwordEncoder.encode(newPw));
        userRepository.save(user);

        result.put("success", true);
        result.put("message", "비밀번호가 성공적으로 변경되었습니다.");
        return result;
    }


    // 개인정보 수정 페이지 (조각)
    @GetMapping("/edit-info")
    public String editInfoPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        return "mypage/fragments/edit-info";
    }

    // 닉네임/이메일/폰 중복 확인 (ajax)
    @GetMapping("/check-nickname")
    @ResponseBody
    public boolean checkNickname(@RequestParam String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return userRepository.existsByEmail(email);
    }

    @GetMapping("/check-phone")
    @ResponseBody
    public boolean checkPhone(@RequestParam String phone) {
        return userRepository.existsByPhone(phone);
    }

    // 개인정보 수정 저장
    @PostMapping("/edit-info")
    @Transactional
    public String updateUserInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute UserEditForm form,
            @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile) {

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            try {
                String uploadsDir = "C:/YomoProject/uploads/";
                File dir = new File(uploadsDir);
                if (!dir.exists()) dir.mkdirs();
                String originalFilename = profileImageFile.getOriginalFilename();
                String safeFileName = System.currentTimeMillis() + "_" + originalFilename;
                File dest = new File(dir, safeFileName);
                profileImageFile.transferTo(dest);
                user.setProfileImageUrl("/uploads/" + safeFileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        user.setNickname(form.getNickname());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        user.setAddressDetail(form.getAddressDetail());
        user.setZipNo(form.getZipNo());

        // 저장 후 마이페이지 내정보(조각) 다시 보여주기
        return "redirect:/mypage?menu=profile";
    }
}

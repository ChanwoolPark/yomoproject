package com.project.yomozomo.controller;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MyPageController {

    private final UserRepository usersRepository;

    @GetMapping({"/", ""})
    public String mypage(Model model, Principal principal) {
        String username = principal.getName(); // email 또는 username
        User user = usersRepository.findByUsername(username).orElseThrow();; // 또는 findByEmail

        model.addAttribute("user", user);  // ★ 이게 안 들어가면 Thymeleaf에서 user.profileImageUrl 못 씀

        return "mypage/layout";
    }

    @GetMapping("/home-summary")
    public String homeSummaryFragment(Model model, Principal principal) {
        String username = principal.getName();
        User user = usersRepository.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);

        return "mypage/fragments/home-summary :: content";
    }


    // ────── 내 정보 ──────
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        User user = usersRepository.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);

        return "mypage/fragments/profile"; // html 전체 X, 조각만 리턴!
    }

    /*@GetMapping("/edit-info")
    public String editInfoFragment() {
        return "mypage/fragments/edit-info :: content";
    }

    @GetMapping("/yomopay")
    public String yomopayFragment() {
        return "mypage/fragments/yomopay :: content";
    }

    // ────── 대여 관리 ──────
    @GetMapping("/rent-list")
    public String rentListFragment() {
        return "mypage/fragments/rent-list :: content";
    }

    @GetMapping("/sale-list")
    public String saleListFragment() {
        return "mypage/fragments/sale-list :: content";
    }

    @GetMapping("/cancel-list")
    public String cancelListFragment() {
        return "mypage/fragments/cancel-list :: content";
    }

    // ────── 내 활동 ──────
    @GetMapping("/wishlist")
    public String wishlistFragment() {
        return "mypage/fragments/wishlist :: content";
    }

    @GetMapping("/recent")
    public String recentFragment() {
        return "mypage/fragments/recent :: content";
    }

    @GetMapping("/reviews")
    public String reviewsFragment() {
        return "mypage/fragments/reviews :: content";
    }

    @GetMapping("/inquiries")
    public String inquiriesFragment() {
        return "mypage/fragments/inquiries :: content";
    }*/
}

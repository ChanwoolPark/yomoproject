package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.domain.ViewedProduct;
import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.dto.UserEditForm;
import com.project.yomozomo.entity.Review;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.ReviewRepository;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.repository.UserWalletRepository;
import com.project.yomozomo.service.ProductListService;
import com.project.yomozomo.service.UserService;
import com.project.yomozomo.service.WalletService;
import com.project.yomozomo.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage")
public class MyPageController {

    private final UserRepository usersRepository;
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final WishlistService wishlistService;
    private final ProductListService productListService;
    private final UserWalletRepository userWalletRepository;
    private final WalletService walletService;



    @GetMapping({"/", ""})
    public String mypage(Model model, Principal principal) {
        String username = principal.getName(); // email 또는 username
        User user = usersRepository.findByUsername(username).orElseThrow();; // 또는 findByEmail


        model.addAttribute("user", user);  // ★ 이게 안 들어가면 Thymeleaf에서 user.profileImageUrl 못 씀

        return "mypage/layout";
    }

    @GetMapping("/home-summary") // 전체 경로: /mypage/home-summary
    public String homeSummaryFragment(Model model, Principal principal) {
        String username = principal.getName();
        User user = usersRepository.findByUsername(username).orElseThrow();
        Long userId = user.getId();
        UserWallet wallet = userWalletRepository.findByUserId(user.getId());
        int userPoint = wallet.getBalance();
        model.addAttribute("user", user);
        model.addAttribute("point", userPoint);

        // 최근 본 상품 5개 추가
        List<ViewedProduct> recent5 = productListService.getRecentlyViewed(userId);
        model.addAttribute("recentlyViewed", recent5);

        return "mypage/fragments/home-summary :: content";
    }


    // ────── 내 정보 ──────
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String username = principal.getName();
        User user = usersRepository.findByUsername(username).orElseThrow();

        // 평점 평균과 리뷰 개수
        double avgRating = reviewRepository.findAverageRatingByTargetId(user.getId()).orElse(0.0);
        int reviewCount = reviewRepository.countByTarget_Id(user.getId());

        user.setRating(BigDecimal.valueOf(avgRating));  // ← 이 한 줄만 추가!

        // 재거래 희망률, 응답률 - 임시 하드코딩 또는 나중에 계산 로직 추가
        int reDealRate = 91; // 예시
        int responseRate = 95;

        // 내가 판매중인 상품 목록
        List<Product> myProducts = productRepository.findBySeller_IdAndStatus(user.getId().intValue(), "판매중");

        // 내가 받은 리뷰 목록
        List<Review> reviews = reviewRepository.findByTargetId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("reDealRate", reDealRate);
        model.addAttribute("responseRate", responseRate);
        model.addAttribute("products", myProducts);
        model.addAttribute("reviews", reviews);

        return "mypage/fragments/profile";
    }

    /*@GetMapping("/yomopay")
    public String yomopayFragment() {
        return "mypage/fragments/yomopay";
    }*/

    // ────── 대여 관리 ──────
    @GetMapping("/rent-list")
    public String rentListFragment() {
        return "mypage/fragments/rent-list";
    }

    @GetMapping("/sale-list")
    public String saleListFragment() {
        return "mypage/fragments/sale-list";
    }

    @GetMapping("/cancel-list")
    public String cancelListFragment() {
        return "mypage/fragments/cancel-list";
    }

    // ────── 내 활동(관심 목록) ──────
    @GetMapping("/wishlist")
    public String userWishlist(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login"; // 로그인 안 되어 있으면 로그인 페이지로
        }

        String username = principal.getName();
        User user = userService.findByUsername(username);
        Long userId = user.getId();

        List<ProductDto> wishlist = wishlistService.getAllWishlist(userId);
        model.addAttribute("wishlist", wishlist);

        return "mypage/fragments/wishlist"; // 관심목록 보여줄 html
    }



    // 최근 본 전체 페이지
    @GetMapping("/recent") // 전체 경로: /mypage/recent
    public String myPageRecentlyViewed(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            Long userId = user.getId();

            List<ViewedProduct> allViewed = productListService.getAllRecentlyViewed(userId);
            model.addAttribute("recentlyViewed", allViewed);
        } else {
            model.addAttribute("recentlyViewed", List.of());
        }

        return "mypage/fragments/recent";
    }

    @GetMapping("/reviews")
    public String reviewsFragment() {
        return "mypage/fragments/reviews";
    }

    @GetMapping("/inquiries")
    public String inquiriesFragment() {
        return "mypage/fragments/inquiries :: content";
    }

    @GetMapping("/{menuName}")
    public String redirectToLayoutWithMenu(@PathVariable String menuName) {
        // 직접 /mypage/profile 등으로 진입하면 → /mypage?menu=profile 로 리디렉트
        return "redirect:/mypage?menu=" + menuName;
    }

    // 📌 개인정보 확인/수정 페이지 이동
    @GetMapping("/edit-info")
    public String editInfoPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        User user = usersRepository.findByUsername(username).orElseThrow();
        model.addAttribute("user", user);
        return "mypage/fragments/edit-info";
    }

    // 📌 닉네임 중복 확인
    @GetMapping("/check-nickname")
    @ResponseBody
    public boolean checkNickname(@RequestParam String nickname) {
        return usersRepository.existsByNickname(nickname);
    }

    // 📌 이메일 중복 확인
    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        return usersRepository.existsByEmail(email);
    }

    // 📌 휴대폰 중복 확인
    @GetMapping("/check-phone")
    @ResponseBody
    public boolean checkPhone(@RequestParam String phone) {
        return usersRepository.existsByPhone(phone);
    }

    @PostMapping("/edit-info")
    @Transactional
    public String updateUserInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute UserEditForm form,
            @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile) {

        User user = usersRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            try {
                // 1. 실제 저장 경로는 C:/YomoProject/uploads/
                String uploadsDir = "C:/YomoProject/uploads/";
                File dir = new File(uploadsDir);
                if (!dir.exists()) dir.mkdirs();

                // 2. 파일명(중복방지)
                String originalFilename = profileImageFile.getOriginalFilename();
                String safeFileName = System.currentTimeMillis() + "_" + originalFilename;
                File dest = new File(dir, safeFileName);

                // 3. 파일 저장
                profileImageFile.transferTo(dest);

                System.out.println("실제 저장 경로: " + dest.getAbsolutePath());

                // 4. DB에는 /uploads/파일명 으로 저장 (★슬래시 주의)
                user.setProfileImageUrl("/uploads/" + safeFileName);

            } catch (Exception e) {
                e.printStackTrace();
                // 필요하면 에러 메시지 보여주기!
            }
        }

        // 나머지 정보 저장
        user.setNickname(form.getNickname());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        user.setAddressDetail(form.getAddressDetail());
        user.setZipNo(form.getZipNo());

        return "redirect:/mypage?menu=profile";
    }





}

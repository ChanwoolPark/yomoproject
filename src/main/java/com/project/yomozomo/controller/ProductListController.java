package com.project.yomozomo.controller;

import com.project.yomozomo.service.ProductListService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/category")
public class ProductListController {

    private final ProductListService productListService;

    public ProductListController(ProductListService productListService) {
        this.productListService = productListService;
    }

    @GetMapping("/{categoryId}")
    public String productList(@PathVariable int categoryId, Model model, HttpSession session) {
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));
        model.addAttribute("products", productListService.getProductsByCategory(categoryId));

        Integer userId = (Integer) session.getAttribute("userId"); // 로그인한 사용자 ID
        if (userId != null) {
            model.addAttribute("wishlist", productListService.getWishlist(userId));
            model.addAttribute("recentlyViewed", productListService.getRecentlyViewed(userId));
        } else {
            model.addAttribute("wishlist", List.of());
            model.addAttribute("recentlyViewed", List.of());
        }

        return "product/list";
    }

    @GetMapping("/test-login")
    public String testLogin(HttpServletRequest request) {
        // Spring Security Context에 인증 객체 수동 삽입
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "testuser", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 세션에도 저장
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
        return "redirect:/category/1"; // 로그인된 상태로 charge 페이지로 리다이렉트
    }

}


package com.project.yomozomo.controller;


import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.ProductImage;
import com.project.yomozomo.domain.User;
import com.project.yomozomo.service.ProductDetailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductDetailService productDetailService;

    public ProductController(ProductDetailService productDetailService) {
        this.productDetailService = productDetailService;
    }

    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable("id") int productId,
                                    Model model,
                                    HttpSession session) {

        // 현재 로그인 유저 ID (세션에서 꺼내기 - 예시)
        Long userId = (Long) session.getAttribute("userId");

        Product product = productDetailService.getProductById(productId);
        List<ProductImage> imageList = productDetailService.getProductImages(productId);

        // 판매자 정보
        User seller = product.getSeller();

        // 날짜 포맷
        String formattedDate = product.getCreatedAt() != null
                ? product.getCreatedAt().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "";

        // 찜 여부 및 채팅방 존재 여부 (로그인 유저가 있을 경우에만)
        boolean isWished = false;
        boolean chatExists = false;

        if (userId != null) {
            isWished = productDetailService.isProductWishedByUser(productId, userId);
        }

        // 모델 전달
        model.addAttribute("product", product);
        model.addAttribute("imageList", imageList);
        model.addAttribute("seller", seller);
        model.addAttribute("createdAt", formattedDate);
        model.addAttribute("wished", isWished);

        return "product/detail"; // detail.html 또는 detail.jsp
    }
}

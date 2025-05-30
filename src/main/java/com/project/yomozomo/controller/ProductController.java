package com.project.yomozomo.controller;


import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.ProductImage;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.ProductDetailService;
import com.project.yomozomo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductDetailService productDetailService;
    private final UserService userService;

    public ProductController(ProductDetailService productDetailService, UserService userService) {
        this.productDetailService = productDetailService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable("id") int productId,
                                    Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Long userId = null;
        if (!"anonymousUser".equals(username)) {
            User user = userService.findByUsername(username);
            userId = user.getId();

            // 👉 최근 본 상품 저장 로직 추가
            productDetailService.saveViewedProduct(userId, productId);
        }

        Product product = productDetailService.incrementViewCount(productId);
        List<ProductImage> imageList = productDetailService.getProductImages(productId);
        User seller = product.getSeller();


        String formattedDate = product.getCreatedAt() != null
                ? product.getCreatedAt().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "";

        boolean isWished = false;
        if (userId != null) {
            isWished = productDetailService.isProductWishedByUser(productId, userId);
        }

        int wishlistCount = productDetailService.getWishlistCount(productId);
        List<Product> otherProducts = productDetailService.getOtherProductsBySeller(seller.getId(), productId);

        model.addAttribute("product", product);
        model.addAttribute("imageList", imageList);
        model.addAttribute("seller", seller);
        model.addAttribute("createdAt", formattedDate);
        model.addAttribute("wished", isWished);
        model.addAttribute("userId", userId);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("otherProducts", otherProducts);

        return "product/detail";
    }
}

package com.project.yomozomo.controller;


import com.project.yomozomo.domain.Category;
import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.ProductImage;
import com.project.yomozomo.entity.Review;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.CategoryService;
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
    private final CategoryService categoryService;

    public ProductController(ProductDetailService productDetailService, UserService userService, CategoryService categoryService) {
        this.productDetailService = productDetailService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable("id") int productId,
                                    Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();


        Long userId = null;
        User user = null;
        String role = null;
        if (!"anonymousUser".equals(username)) {
            user = userService.findByUsername(username);
            userId = user.getId();
            role = user.getRole();

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
        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();

        // ⭐️ 리뷰 가져오기 추가
        List<Review> productReviews = productDetailService.findReviewsByProductId((long) productId);

        model.addAttribute("product", product);
        model.addAttribute("imageList", imageList);
        model.addAttribute("seller", seller);
        model.addAttribute("createdAt", formattedDate);
        model.addAttribute("wished", isWished);
        model.addAttribute("userId", userId);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("otherProducts", otherProducts);
        model.addAttribute("categories", categories);
        model.addAttribute("productReviews", productReviews); // ✅ 추가!
        model.addAttribute("isLoggedIn", user != null);
        model.addAttribute("userRole", role);

        return "product/detail";
    }

}

package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.ProductListService;
import com.project.yomozomo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/category")
public class ProductListController {

    private final ProductListService productListService;
    private final UserService userService;

    public ProductListController(ProductListService productListService, UserService userService) {
        this.productListService = productListService;
        this.userService = userService;
    }

    // [1] 카테고리별 상품 목록
    @GetMapping("/{categoryId}")
    public String productList(@PathVariable int categoryId, Model model, Principal principal) {
        // 카테고리 객체 생성 및 설정
        Category category = new Category();
        category.setCategoryId(categoryId);

        // 모델에 데이터 추가
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));
        model.addAttribute("products", productListService.getProductsByCategory(categoryId));
        model.addAttribute("category", category);

        addUserRelatedAttributes(model, principal);
        return "product/list";
    }

    @GetMapping("/{categoryId}/subcategory/{subCategoryId}")
    public String productListBySubCategory(@PathVariable int categoryId,
                                           @PathVariable int subCategoryId,
                                           Model model,
                                           Principal principal) {
        // 카테고리 객체 생성 및 설정
        Category category = new Category();
        category.setCategoryId(categoryId);

        // 모델에 데이터 추가
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));
        model.addAttribute("products", productListService.getProductsBySubCategoryId(subCategoryId));
        model.addAttribute("category", category);

        addUserRelatedAttributes(model, principal);
        return "product/list";
    }

    // [3] 로그인 시 최근 본 상품/찜 목록
    private void addUserRelatedAttributes(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userService.findByUsername(username);
            Long userId = user.getId();

            model.addAttribute("wishlist", productListService.getWishlist(userId));
            model.addAttribute("recentlyViewed", productListService.getRecentlyViewed(userId));
        } else {
            model.addAttribute("wishlist", List.of());
            model.addAttribute("recentlyViewed", List.of());
        }
    }
}
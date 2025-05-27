package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.service.ProductListService;
import jakarta.servlet.http.HttpSession;
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

        List<ProductDto> products = productListService.getProductsByCategory(categoryId);
        model.addAttribute("products", products);

        Category category = new Category();
        category.setCategoryId(categoryId);
        model.addAttribute("category", category);

        addUserRelatedAttributes(model, session);

        return "product/list";
    }

    @GetMapping("/{categoryId}/subcategory/{subCategoryId}")
    public String productListBySubCategory(@PathVariable int categoryId,
                                           @PathVariable int subCategoryId,
                                           Model model,
                                           HttpSession session) {
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));

        List<ProductDto> products = productListService.getProductsBySubCategoryId(subCategoryId);
        model.addAttribute("products", products);

        Category category = new Category();
        category.setCategoryId(categoryId);
        model.addAttribute("category", category);

        addUserRelatedAttributes(model, session);

        return "product/list";
    }

    // 중복 제거: 로그인 사용자 관련 속성 처리
    private void addUserRelatedAttributes(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            model.addAttribute("wishlist", productListService.getWishlist(userId));
            model.addAttribute("recentlyViewed", productListService.getRecentlyViewed(userId));
        } else {
            model.addAttribute("wishlist", List.of());
            model.addAttribute("recentlyViewed", List.of());
        }
    }
}
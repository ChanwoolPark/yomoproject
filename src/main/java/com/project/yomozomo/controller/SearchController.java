package com.project.yomozomo.controller;

import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.CategoryService;
import com.project.yomozomo.service.ProductListService;
import com.project.yomozomo.service.SearchService;
import com.project.yomozomo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class SearchController {

    private final CategoryService categoryService;
    private final SearchService searchService;
    private final UserService userService;
    private final ProductListService productListService;

    public SearchController(CategoryService categoryService, SearchService searchService
    , UserService userService, ProductListService productListService) {
        this.categoryService = categoryService;
        this.searchService = searchService;
        this.userService = userService;
        this.productListService = productListService;
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) Integer categoryId,
                         @RequestParam(required = false) String keyword,
                         Model model,
                         Principal principal) {

        model.addAttribute("isSearch", true);
        model.addAttribute("categories", categoryService.getAllCategoriesWithSubCategories());
        if (categoryId != null) {
            model.addAttribute("subCategories", productListService.getSubCategories(categoryId));
        } else {
            model.addAttribute("subCategories", List.of()); // 전체 검색 시에는 안 보이게
        }

        List<ProductDto> products = (categoryId != null)
                ? searchService.searchByCategory(categoryId, keyword)
                : searchService.searchAll(keyword);

        model.addAttribute("products", products);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("keyword", keyword);

        // 최근 본 상품, 찜 목록도 동일하게 구성
        if (principal != null) {
            String username = principal.getName();
            User



                    user = userService.findByUsername(username);
            Long userId = user.getId();

            model.addAttribute("wishlist", productListService.getWishlist(userId));
            model.addAttribute("recentlyViewed", productListService.getRecentlyViewed(userId));
        } else {
            model.addAttribute("wishlist", List.of());
            model.addAttribute("recentlyViewed", List.of());
        }

        return "product/list";
    }
}

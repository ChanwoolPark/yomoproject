package com.project.yomozomo.controller.index;

import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.CategoryService;
import com.project.yomozomo.service.ProductListService;
import com.project.yomozomo.service.SearchService;
import com.project.yomozomo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
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
                         @RequestParam(required = false) String sourceTab,
                         Model model,
                         Principal principal,
                         HttpSession session) {

        // 🔍 최근 검색어 세션 저장
        if (keyword != null && !keyword.isBlank()) {
            List<String> recentKeywords = (List<String>) session.getAttribute("recentKeywords");
            if (recentKeywords == null) recentKeywords = new ArrayList<>();

            recentKeywords.remove(keyword);
            recentKeywords.add(0, keyword);
            if (recentKeywords.size() > 10) recentKeywords = recentKeywords.subList(0, 10);

            session.setAttribute("recentKeywords", recentKeywords);
        }

        model.addAttribute("isSearch", true);
        model.addAttribute("isPriceFiltered", false); // 검색 시 정렬 비활성화

        model.addAttribute("categories", categoryService.getAllCategoriesWithSubCategories());
        if (categoryId != null) {
            model.addAttribute("category", categoryService.getCategoryById(categoryId));
            model.addAttribute("subCategories", productListService.getSubCategories(categoryId));
        } else {
            model.addAttribute("category", null);
            model.addAttribute("subCategories", List.of());
        }

        List<ProductDto> products = (categoryId != null)
                ? searchService.searchByCategory(categoryId, keyword)
                : searchService.searchAll(keyword);

        model.addAttribute("products", products);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sourceTab", sourceTab != null ? sourceTab : "recent");

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

        return "product/list";
    }
}

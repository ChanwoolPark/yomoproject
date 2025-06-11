package com.project.yomozomo.controller.product;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.CategoryService;
import com.project.yomozomo.service.ProductListService;
import com.project.yomozomo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/category")
public class ProductListController {

    private final ProductListService productListService;
    private final UserService userService;
    private final CategoryService categoryService;

    public ProductListController(ProductListService productListService, UserService userService, CategoryService categoryService) {
        this.productListService = productListService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    // 카테고리별 상품 목록
    @GetMapping("/{categoryId}")
    public String productList(@PathVariable int categoryId,
                              @RequestParam(defaultValue = "latest") String sort,
                              @RequestParam(required = false) Integer minPrice,
                              @RequestParam(required = false) Integer maxPrice,
                              Model model,
                              Principal principal) {

        model.addAttribute("isSearch", false);
        model.addAttribute("isPriceFiltered", (minPrice != null || maxPrice != null)); // ✅ 뷰에서 정렬 숨기기 조건

        Category category = new Category();
        category.setCategoryId(categoryId);

        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));
        model.addAttribute("category", category);
        model.addAttribute("subCategoryId", null); // 뷰에서 구분용

        List<ProductDto> products;

        if (minPrice != null || maxPrice != null) {
            products = productListService.getProductsByCategoryAndPriceRange(categoryId, minPrice, maxPrice);
        } else {
            products = productListService.getProductsByCategorySorted(categoryId, sort);
        }

        model.addAttribute("products", products);
        model.addAttribute("currentSort", sort);
        model.addAttribute("param", Map.of( // ✅ 가격 필터 유지용
                "minPrice", minPrice != null ? minPrice : "",
                "maxPrice", maxPrice != null ? maxPrice : ""
        ));

        addUserRelatedAttributes(model, principal);
        return "product/list";
    }
    // 서브 카테고리 선택시 상품 목록
    @GetMapping("/{categoryId}/subcategory/{subCategoryId}")
    public String productListBySubCategory(@PathVariable int categoryId,
                                           @PathVariable int subCategoryId,
                                           @RequestParam(required = false) Integer minPrice,
                                           @RequestParam(required = false) Integer maxPrice,
                                           @RequestParam(defaultValue = "latest") String sort,
                                           Model model,
                                           Principal principal) {

        boolean isPriceFiltered = (minPrice != null || maxPrice != null);
        model.addAttribute("isPriceFiltered", isPriceFiltered);
        model.addAttribute("isSearch", false);

        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();
        model.addAttribute("categories", categories);

        Category category = new Category();
        category.setCategoryId(categoryId);
        model.addAttribute("category", category);
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));

        if (isPriceFiltered) {
            model.addAttribute("products", productListService.getProductsBySubCategoryAndPriceRange(subCategoryId, minPrice, maxPrice));
        } else {
            model.addAttribute("products", productListService.getProductsBySubCategorySorted(subCategoryId, sort));
        }

        model.addAttribute("currentSort", sort);

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
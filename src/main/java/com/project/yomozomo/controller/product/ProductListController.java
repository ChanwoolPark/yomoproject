package com.project.yomozomo.controller.product;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.dto.ProductPageDto;
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

@Controller
@RequestMapping("/category")
public class ProductListController {

    private final ProductListService productListService;
    private final UserService userService;
    private final CategoryService categoryService;
    private static final int BLOCK_SIZE = 10; // 🔹 여기 추가

    public ProductListController(ProductListService productListService, UserService userService, CategoryService categoryService) {
        this.productListService = productListService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    // ✅ 카테고리별 상품 목록
    @GetMapping("/{categoryId}")
    public String productList(@PathVariable int categoryId,
                              @RequestParam(defaultValue = "latest") String sort,
                              @RequestParam(required = false) Integer minPrice,
                              @RequestParam(required = false) Integer maxPrice,
                              @RequestParam(defaultValue = "0") int page,
                              Model model,
                              Principal principal) {

        model.addAttribute("isSearch", false);
        model.addAttribute("isPriceFiltered", (minPrice != null || maxPrice != null));

        Category category = new Category();
        category.setCategoryId(categoryId);

        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));
        model.addAttribute("category", category);
        model.addAttribute("subCategoryId", null);

        ProductPageDto pageResult = (minPrice != null || maxPrice != null)
                ? productListService.getCategoryPriceFilteredPage(categoryId, minPrice, maxPrice, page)
                : productListService.getCategorySortedPage(categoryId, sort, page);

        setPagingModel(model, page, pageResult.totalPages());

        model.addAttribute("products", pageResult.products());
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);

        model.addAttribute("minPrice", minPrice != null ? minPrice : "");
        model.addAttribute("maxPrice", maxPrice != null ? maxPrice : "");

        addUserRelatedAttributes(model, principal);
        return "product/list";
    }

    @GetMapping("/{categoryId}/subcategory/{subCategoryId}")
    public String productListBySubCategory(@PathVariable int categoryId,
                                           @PathVariable int subCategoryId,
                                           @RequestParam(required = false) Integer minPrice,
                                           @RequestParam(required = false) Integer maxPrice,
                                           @RequestParam(defaultValue = "latest") String sort,
                                           @RequestParam(defaultValue = "0") int page,
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
        model.addAttribute("subCategoryId", subCategoryId);

        ProductPageDto pageResult = isPriceFiltered
                ? productListService.getSubCategoryPriceFilteredPage(subCategoryId, minPrice, maxPrice, page)
                : productListService.getSubCategorySortedPage(subCategoryId, sort, page);

        setPagingModel(model, page, pageResult.totalPages());

        model.addAttribute("products", pageResult.products());
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentPage", page);

        model.addAttribute("minPrice", minPrice != null ? minPrice : "");
        model.addAttribute("maxPrice", maxPrice != null ? maxPrice : "");

        addUserRelatedAttributes(model, principal);
        return "product/list";
    }


    // ✅ 유저 관련 정보 (찜, 최근 본 상품)
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

    private void setPagingModel(Model model, int currentPage, int totalPages) {
        int startPage = (currentPage / BLOCK_SIZE) * BLOCK_SIZE;
        int endPage = Math.min(startPage + BLOCK_SIZE, totalPages);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrevBlock", startPage > 0);
        model.addAttribute("hasNextBlock", endPage < totalPages);
    }
}

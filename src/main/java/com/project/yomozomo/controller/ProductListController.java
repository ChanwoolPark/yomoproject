package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.ProductListService;
import com.project.yomozomo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{categoryId}")
    public String productList(@PathVariable int categoryId, Model model, Principal principal) {
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));

        List<ProductDto> products = productListService.getProductsByCategory(categoryId);
        model.addAttribute("products", products);

        Category category = new Category();
        category.setCategoryId(categoryId);
        model.addAttribute("category", category);

        addUserRelatedAttributes(model, principal);

        return "product/list";
    }

    @GetMapping("/{categoryId}/subcategory/{subCategoryId}")
    public String productListBySubCategory(@PathVariable int categoryId,
                                           @PathVariable int subCategoryId,
                                           Model model,
                                           Principal principal) {
        model.addAttribute("subCategories", productListService.getSubCategories(categoryId));

        List<ProductDto> products = productListService.getProductsBySubCategoryId(subCategoryId);
        model.addAttribute("products", products);

        Category category = new Category();
        category.setCategoryId(categoryId);
        model.addAttribute("category", category);

        addUserRelatedAttributes(model, principal);

        return "product/list";
    }

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
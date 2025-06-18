package com.project.yomozomo.controller.user;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserPageController {

    private final CategoryService categoryService;

    public UserPageController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/find-id")
    public String showFindIdPage(Model model) {
        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();
        model.addAttribute("categories", categories);
        return "find-id";
    }

    @GetMapping("/find-password")
    public String showFindPasswordPage(Model model) {
        List<Category> categories = categoryService.getAllCategoriesWithSubCategories();
        model.addAttribute("categories", categories);
        return "find-password";
    }
}
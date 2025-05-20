package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class HeaderController {

    private final CategoryService categoryService;

    public HeaderController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 모든 뷰에 header 데이터로 카테고리 리스트를 넣고 싶다면 @ModelAttribute 활용
    @ModelAttribute("categories")
    public List<Category> getAllCategories() {
        System.out.println("카테고리 불러오기");
        return categoryService.getAllCategories(); // 여기가 null 리턴하면 안 됨
    }

}

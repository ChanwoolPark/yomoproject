package com.project.yomozomo.service;

import com.project.yomozomo.domain.Category;
import com.project.yomozomo.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getAllCategoriesWithSubCategories() {
        List<Category> categories = categoryRepository.findAllWithSubCategories();
        categories.sort(Comparator.comparing(Category::getCategoryId));

        return categories;
    }
}
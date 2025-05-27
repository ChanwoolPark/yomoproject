package com.project.yomozomo.service;

import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.repository.SubCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductFormService {

    private final SubCategoryRepository subCategoryRepository;

    public ProductFormService(SubCategoryRepository subCategoryRepository) {
        this.subCategoryRepository = subCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<SubCategory> getAllSubCategories(int categoryId) {
        return subCategoryRepository.findByCategory_CategoryId(categoryId);
    }
}

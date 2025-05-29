package com.project.yomozomo.service;

import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.repository.SubCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;

    public SubCategory findById(int subCategoryId) {
        return subCategoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new RuntimeException("서브카테고리를 찾을 수 없습니다."));
    }
}
package com.project.yomozomo.repository;

import com.project.yomozomo.domain.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {
    List<SubCategory> findByCategory_CategoryId(int categoryId);
}

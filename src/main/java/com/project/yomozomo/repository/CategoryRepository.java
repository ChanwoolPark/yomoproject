package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // JpaRepository가 기본 CRUD 메서드 제공
}
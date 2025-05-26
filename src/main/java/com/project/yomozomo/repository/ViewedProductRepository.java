package com.project.yomozomo.repository;

import com.project.yomozomo.domain.ViewedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ViewedProductRepository extends JpaRepository<ViewedProduct, Integer> {
    List<ViewedProduct> findTop5ByUserIdOrderByViewedAtDesc(Long userId);
}
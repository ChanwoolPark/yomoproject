package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.ViewedProduct;
import com.project.yomozomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ViewedProductRepository extends JpaRepository<ViewedProduct, Integer> {
    List<ViewedProduct> findTop5ByUserIdOrderByViewedAtDesc(Long userId);

    Optional<ViewedProduct> findByUserAndProduct(User user, Product product);

    List<ViewedProduct> findByUserOrderByViewedAtDesc(User user);
    List<ViewedProduct> findByUserIdOrderByViewedAtDesc(Long userId);
}
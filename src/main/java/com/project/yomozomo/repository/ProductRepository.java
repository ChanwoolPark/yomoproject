package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findBySubCategory_Category_CategoryId(int categoryId);
    List<Product> findBySubCategory_SubCategoryId(int subCategoryId);

    List<Product> findBySeller_IdAndStatus(int seller, String status);
    List<Product> findBySeller_Id(Long sellerId);




    // is_deleted = 'N' 조건 추가
    List<Product> findBySubCategory_Category_CategoryIdAndIsDeleted(int categoryId, String isDeleted);
    List<Product> findBySubCategory_SubCategoryIdAndIsDeleted(int subCategoryId, String isDeleted);
}
package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findBySubCategory_Category_CategoryId(int categoryId);
    List<Product> findBySubCategory_SubCategoryId(int subCategoryId);

    List<Product> findBySeller_IdAndStatus(int seller, String status);
    List<Product> findBySeller_Id(Long sellerId);
    List<Product> findBySeller_IdAndStatusIn(Long sellerId, List<String> statusList);




    // is_deleted = 'N' 조건 추가
    List<Product> findBySubCategory_Category_CategoryIdAndIsDeleted(int categoryId, String isDeleted);
    List<Product> findBySubCategory_SubCategoryIdAndIsDeleted(int subCategoryId, String isDeleted);
    List<Product> findBySeller_IdAndIsDeletedAndProductIdNot(Long sellerId, String isDeleted, int excludeProductId);

    // 검색창
    List<Product> findByTitleContainingIgnoreCaseAndIsDeleted(String keyword, String isDeleted);
    List<Product> findByIsDeleted(String isDeleted);
    List<Product> findBySubCategory_Category_CategoryIdAndTitleContainingIgnoreCaseAndIsDeleted(int categoryId, String keyword, String isDeleted);

    @Query("SELECT p.title FROM Product p WHERE p.isDeleted = 'N' ORDER BY p.count DESC")
    List<String> findTop10PopularTitles(Pageable pageable);
}
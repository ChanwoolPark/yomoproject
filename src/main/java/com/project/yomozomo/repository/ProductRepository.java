package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

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


    /*가격 필터 (대카테고리용)*/
    @Query("""
    SELECT p FROM Product p
    WHERE p.subCategory.category.categoryId = :categoryId
    AND p.isDeleted = 'N'
    AND (:minPrice IS NULL OR p.price >= :minPrice)
    AND (:maxPrice IS NULL OR p.price <= :maxPrice)
""")
    List<Product> findByCategoryWithPriceFilter(
            @org.springframework.lang.NonNull Integer categoryId,
            @org.springframework.lang.Nullable Integer minPrice,
            @org.springframework.lang.Nullable Integer maxPrice
    );
    /*가격 필터 (서브카테고리용)*/
    @Query("""
    SELECT p FROM Product p
    WHERE p.subCategory.subCategoryId = :subCategoryId
    AND p.isDeleted = 'N'
    AND (:minPrice IS NULL OR p.price >= :minPrice)
    AND (:maxPrice IS NULL OR p.price <= :maxPrice)
""")
    List<Product> findBySubCategoryWithPriceFilter(
            @org.springframework.lang.NonNull Integer subCategoryId,
            @org.springframework.lang.Nullable Integer minPrice,
            @org.springframework.lang.Nullable Integer maxPrice
    );

    /*정렬 쿼리(대카테고리용)*/
    @Query("SELECT p FROM Product p WHERE p.subCategory.category.categoryId = :categoryId AND p.isDeleted = 'N' ORDER BY p.createdAt DESC")
    List<Product> findByCategoryOrderByCreatedAtDesc(@org.springframework.lang.NonNull Integer categoryId);

    @Query("SELECT p FROM Product p WHERE p.subCategory.category.categoryId = :categoryId AND p.isDeleted = 'N' ORDER BY p.count DESC")
    List<Product> findByCategoryOrderByCountDesc(@org.springframework.lang.NonNull Integer categoryId);

    @Query("SELECT p FROM Product p WHERE p.subCategory.category.categoryId = :categoryId AND p.isDeleted = 'N' ORDER BY p.price ASC")
    List<Product> findByCategoryOrderByPriceAsc(@org.springframework.lang.NonNull Integer categoryId);
    /*정렬 쿼리(서브카테고리용)*/
    @Query("SELECT p FROM Product p WHERE p.subCategory.subCategoryId = :subCategoryId AND p.isDeleted = 'N' ORDER BY p.createdAt DESC")
    List<Product> findBySubCategoryOrderByCreatedAtDesc(@NonNull Integer subCategoryId);

    @Query("SELECT p FROM Product p WHERE p.subCategory.subCategoryId = :subCategoryId AND p.isDeleted = 'N' ORDER BY p.count DESC")
    List<Product> findBySubCategoryOrderByCountDesc(@NonNull Integer subCategoryId);

    @Query("SELECT p FROM Product p WHERE p.subCategory.subCategoryId = :subCategoryId AND p.isDeleted = 'N' ORDER BY p.price ASC")
    List<Product> findBySubCategoryOrderByPriceAsc(@NonNull Integer subCategoryId);

}
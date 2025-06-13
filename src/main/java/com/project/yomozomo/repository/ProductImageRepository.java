package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    // 상품 목록에서 대표이미지 조회
    @Query(value = "SELECT pi.image_url FROM product_image pi " +
            "WHERE pi.product_id = :productId " +
            "ORDER BY pi.image_id ASC FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    String findTopImageUrlByProductId(@Param("productId") Long productId);

    // 상세 페이지용: 전체 이미지 조회
    List<ProductImage> findByProductProductId(int productId);

    // 기존 이미지 삭제용 메서드
    void deleteByProduct(Product product);
}
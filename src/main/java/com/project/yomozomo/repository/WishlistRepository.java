package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByUserId(Long userId);

    // 찜 여부 확인용
    boolean existsByProduct_ProductIdAndUser_Id(int productId, Long userId);

}

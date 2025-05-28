package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Wishlist;
import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistService(WishlistRepository wishlistRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public boolean addToWishlist(Long userId, int productId) {
        boolean alreadyExists = wishlistRepository.existsByProduct_ProductIdAndUser_Id(productId, userId);
        if (alreadyExists) {
            return false;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다"));

        Wishlist wishlist = new Wishlist();
        wishlist.setProduct(product);
        wishlist.setUser(user);

        wishlistRepository.save(wishlist);
        return true;
    }

    // 전체 관심목록 가져오기
    public List<ProductDto> getAllWishlist(Long userId) {
        List<Wishlist> wishlist = wishlistRepository.findByUserId(userId);

        return wishlist.stream()
                .sorted((w1, w2) -> w2.getLikedDate().compareTo(w1.getLikedDate())) // 최신순
                .map(w -> {
                    Product p = w.getProduct();
                    return new ProductDto(p.getProductId(), p.getTitle(), p.getThumbnailUrl());
                })
                .toList();
    }

    // 찜하기 취소(데이터베이스 삭제)
    @Transactional
    public void removeFromWishlist(Long userId, int productId) {
        wishlistRepository.deleteByUser_IdAndProduct_ProductId(userId, productId);
    }

}

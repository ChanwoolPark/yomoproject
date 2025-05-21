package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.ProductImage;
import com.project.yomozomo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductDetailService {

    private final ProductRepository productRepo;
    private final ProductImageRepository productImageRepo;
    private final UserRepository userRepo;
    private final WishlistRepository wishlistRepo;
    private final ChatRoomRepository chatRoomRepo;

    public ProductDetailService(ProductRepository productRepo,
                                ProductImageRepository productImageRepo,
                                UserRepository userRepo,
                                WishlistRepository wishlistRepo,
                                ChatRoomRepository chatRoomRepo) {
        this.productRepo = productRepo;
        this.productImageRepo = productImageRepo;
        this.userRepo = userRepo;
        this.wishlistRepo = wishlistRepo;
        this.chatRoomRepo = chatRoomRepo;
    }

    @Transactional(readOnly = true)
    public Product getProductById(int productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(int productId) {
        return productImageRepo.findByProductProductId(productId);
    }

    @Transactional(readOnly = true)
    public boolean isProductWishedByUser(int productId, Long userId) {
        return wishlistRepo.existsByProduct_ProductIdAndUser_Id(productId, userId);
    }

    @Transactional(readOnly = true)
    public boolean existsChatRoomBetweenUsers(Long sellerId, Long buyerId) {
        return chatRoomRepo.existsByBuyer_IdAndSeller_IdOrBuyer_IdAndSeller_Id(
                buyerId, sellerId, sellerId, buyerId
        );
    }
}

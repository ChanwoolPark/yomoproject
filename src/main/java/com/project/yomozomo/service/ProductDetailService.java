package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.ProductImage;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.domain.ViewedProduct;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductDetailService {

    private final ProductRepository productRepo;
    private final ProductImageRepository productImageRepo;
    private final UserRepository userRepo;
    private final WishlistRepository wishlistRepo;
    private final ChatRoomRepository chatRoomRepo;
    private final RentalRepository rentalRepo;
    private final ViewedProductRepository viewedProductRepo;

    public ProductDetailService(ProductRepository productRepo,
                                ProductImageRepository productImageRepo,
                                UserRepository userRepo,
                                WishlistRepository wishlistRepo,
                                ChatRoomRepository chatRoomRepo, RentalRepository rentalRepo,
                                ViewedProductRepository viewedProductRepo) {
        this.productRepo = productRepo;
        this.productImageRepo = productImageRepo;
        this.userRepo = userRepo;
        this.wishlistRepo = wishlistRepo;
        this.chatRoomRepo = chatRoomRepo;
        this.rentalRepo = rentalRepo;
        this.viewedProductRepo = viewedProductRepo;
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

    @Transactional
    public Product incrementViewCount(int productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        int currentCount = product.getCount();
        System.out.println("[조회수 증가 전] count = " + currentCount);

        product.setCount(currentCount + 1);
        productRepo.save(product);

        System.out.println("[조회수 증가 후] count = " + product.getCount());

        return product; // ✔️ 조회된 product 리턴
    }


    @Transactional(readOnly = true)
    public List<Rental> getReservedDates(int productId) {
        List<String> statusList = List.of("예약", "대여중");
        return rentalRepo.findByProduct_ProductIdAndStatusIn(productId, statusList);
    }

    @Transactional
    public void saveViewedProduct(Long userId, int productId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 중복 제거: 이미 본 상품이 있으면 삭제
        viewedProductRepo.findByUserAndProduct(user, product)
                .ifPresent(viewedProductRepo::delete);

        // 새로 저장
        ViewedProduct newView = new ViewedProduct();
        newView.setUser(user);
        newView.setProduct(product);
        newView.setViewedAt(LocalDateTime.now());
        viewedProductRepo.save(newView);

        // 최대 5개 유지: 6개 이상이면 오래된 것 삭제
        List<ViewedProduct> views = viewedProductRepo.findByUserOrderByViewedAtDesc(user);
        if (views.size() > 5) {
            List<ViewedProduct> toDelete = views.subList(5, views.size());
            viewedProductRepo.deleteAll(toDelete);
        }
    }

}

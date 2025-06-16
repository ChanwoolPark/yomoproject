package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.domain.ViewedProduct;
import com.project.yomozomo.domain.Wishlist;
import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.dto.ProductPageDto;
import com.project.yomozomo.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProductListService {

    private final SubCategoryRepository subCategoryRepo;
    private final ProductRepository productRepo;
    private final WishlistRepository wishlistRepo;
    private final ViewedProductRepository viewedRepo;
    private final ProductImageRepository productImageRepo;
    private static final String NOT_DELETED = "N";

    public ProductListService(SubCategoryRepository subCategoryRepo,
                              ProductRepository productRepo,
                              WishlistRepository wishlistRepo,
                              ViewedProductRepository viewedRepo,
                              ProductImageRepository productImageRepo) {
        this.subCategoryRepo = subCategoryRepo;
        this.productRepo = productRepo;
        this.wishlistRepo = wishlistRepo;
        this.viewedRepo = viewedRepo;
        this.productImageRepo = productImageRepo;
    }

    @Transactional(readOnly = true)
    public List<SubCategory> getSubCategories(int categoryId) {
        return subCategoryRepo.findByCategory_CategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getWishlist(Long userId) {
        List<Wishlist> wishlist = wishlistRepo.findByUserId(userId);

        return wishlist.stream()
                .sorted((w1, w2) -> w2.getLikedDate().compareTo(w1.getLikedDate()))
                .limit(5)
                .map(w -> {
                    Product p = w.getProduct();
                    return new ProductDto(p.getProductId(), p.getTitle(), p.getThumbnailUrl());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ViewedProduct> getRecentlyViewed(Long userId) {
        return viewedRepo.findTop5ByUserIdOrderByViewedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<ViewedProduct> getAllRecentlyViewed(Long userId) {
        return viewedRepo.findByUserIdOrderByViewedAtDesc(userId);
    }

    private List<ProductDto> mapProductsToDto(List<Product> products) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return products.stream()
                .map(product -> {
                    String imageUrl = productImageRepo.findTopImageUrlByProductId(product.getProductId());

                    String formattedDate = null;
                    if (product.getCreatedAt() != null) {
                        formattedDate = product.getCreatedAt().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(formatter);
                    }

                    return new ProductDto(
                            product.getProductId(),
                            product.getTitle(),
                            product.getPrice(),
                            product.getDeposit(),
                            formattedDate,
                            imageUrl
                    );
                })
                .toList();
    }

    private List<ProductDto> mapProductsToDto(Page<Product> page) {
        return mapProductsToDto(page.getContent());
    }

    @Transactional(readOnly = true)
    public ProductPageDto getCategoryPriceFilteredPage(int categoryId, Integer minPrice, Integer maxPrice, int page) {
        Pageable pageable = PageRequest.of(page, 28);
        Page<Product> productPage = productRepo.findByCategoryWithPriceFilter(categoryId, minPrice, maxPrice, pageable);

        System.out.println("전체 페이지 수: " + productPage.getTotalPages());
        System.out.println("전체 상품 수: " + productPage.getTotalElements());
        System.out.println("현재 페이지: " + productPage.getNumber());
        return new ProductPageDto(mapProductsToDto(productPage), productPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductPageDto getSubCategoryPriceFilteredPage(int subCategoryId, Integer minPrice, Integer maxPrice, int page) {
        Pageable pageable = PageRequest.of(page, 28);
        Page<Product> productPage = productRepo.findBySubCategoryWithPriceFilter(subCategoryId, minPrice, maxPrice, pageable);
        return new ProductPageDto(mapProductsToDto(productPage), productPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductPageDto getCategorySortedPage(int categoryId, String sort, int page) {
        Pageable pageable = PageRequest.of(page, 28);
        Page<Product> productPage = switch (sort) {
            case "views" -> productRepo.findByCategoryOrderByCountDesc(categoryId, pageable);
            case "price" -> productRepo.findByCategoryOrderByPriceAsc(categoryId, pageable);
            default -> productRepo.findByCategoryOrderByCreatedAtDesc(categoryId, pageable);
        };

        System.out.println("전체 페이지 수: " + productPage.getTotalPages());
        System.out.println("전체 상품 수: " + productPage.getTotalElements());
        System.out.println("현재 페이지: " + productPage.getNumber());
        return new ProductPageDto(mapProductsToDto(productPage), productPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ProductPageDto getSubCategorySortedPage(int subCategoryId, String sort, int page) {
        Pageable pageable = PageRequest.of(page, 28);
        Page<Product> productPage = switch (sort) {
            case "views" -> productRepo.findBySubCategoryOrderByCountDesc(subCategoryId, pageable);
            case "price" -> productRepo.findBySubCategoryOrderByPriceAsc(subCategoryId, pageable);
            default -> productRepo.findBySubCategoryOrderByCreatedAtDesc(subCategoryId, pageable);
        };
        return new ProductPageDto(mapProductsToDto(productPage), productPage.getTotalPages());
    }
}

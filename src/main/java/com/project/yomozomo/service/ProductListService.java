package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.domain.ViewedProduct;
import com.project.yomozomo.domain.Wishlist;
import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.repository.*;
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
    public List<ProductDto> getProductsByCategory(int categoryId) {
        List<Product> products = productRepo.findBySubCategory_Category_CategoryIdAndIsDeleted(categoryId, NOT_DELETED);
        return mapProductsToDto(products);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsBySubCategoryId(int subCategoryId) {
        List<Product> products = productRepo.findBySubCategory_SubCategoryIdAndIsDeleted(subCategoryId, NOT_DELETED);
        return mapProductsToDto(products);
    }

    @Transactional(readOnly = true)
    public List<Wishlist> getWishlist(Long userId) {
        return wishlistRepo.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<ViewedProduct> getRecentlyViewed(Long userId) {
        return viewedRepo.findTop5ByUserIdOrderByViewedAtDesc(userId);
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
}

package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.SubCategory;
import com.project.yomozomo.domain.ViewedProduct;
import com.project.yomozomo.domain.Wishlist;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.SubCategoryRepository;
import com.project.yomozomo.repository.ViewedProductRepository;
import com.project.yomozomo.repository.WishlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductListService {
    private final SubCategoryRepository subCategoryRepo;
    private final ProductRepository productRepo;
    private final WishlistRepository wishlistRepo;
    private final ViewedProductRepository viewedRepo;

    public ProductListService(SubCategoryRepository subCategoryRepo, ProductRepository productRepo,
                              WishlistRepository wishlistRepo, ViewedProductRepository viewedRepo) {
        this.subCategoryRepo = subCategoryRepo;
        this.productRepo = productRepo;
        this.wishlistRepo = wishlistRepo;
        this.viewedRepo = viewedRepo;
    }

    public List<SubCategory> getSubCategories(int categoryId) {
        return subCategoryRepo.findByCategory_CategoryId(categoryId);
    }

    public List<Product> getProductsByCategory(int categoryId) {
        return productRepo.findBySubCategory_Category_CategoryId(categoryId);
    }

    public List<Wishlist> getWishlist(int userId) {
        return wishlistRepo.findByUserId(userId);
    }

    public List<ViewedProduct> getRecentlyViewed(int userId) {
        return viewedRepo.findTop5ByUserIdOrderByViewedAtDesc(userId);
    }
}
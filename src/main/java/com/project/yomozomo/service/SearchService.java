package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.dto.ProductDto;
import com.project.yomozomo.repository.ProductImageRepository;
import com.project.yomozomo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
public class SearchService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepo;
    private static final String NOT_DELETED = "N";

    public SearchService(ProductRepository productRepository, ProductImageRepository productImageRepo) {
        this.productRepository = productRepository;
        this.productImageRepo = productImageRepo;
    }

    public List<ProductDto> searchByCategory(int categoryId, String keyword) {
        List<Product> products;

        if (keyword != null && !keyword.isBlank()) {
            products = productRepository
                    .findBySubCategory_Category_CategoryIdAndTitleContainingIgnoreCaseAndIsDeleted(
                            categoryId, keyword, NOT_DELETED);
        } else {
            products = productRepository
                    .findBySubCategory_Category_CategoryIdAndIsDeleted(categoryId, NOT_DELETED);
        }

        return mapToDto(products);
    }

    public List<ProductDto> searchAll(String keyword) {
        List<Product> products;

        if (keyword != null && !keyword.isBlank()) {
            products = productRepository
                    .findByTitleContainingIgnoreCaseAndIsDeleted(keyword, NOT_DELETED);
        } else {
            products = productRepository
                    .findByIsDeleted(NOT_DELETED);
        }

        return mapToDto(products);
    }

    private List<ProductDto> mapToDto(List<Product> products) {
        return products.stream()
                .map(p -> new ProductDto(
                        p.getProductId(),
                        p.getTitle(),
                        p.getPrice(),
                        p.getDeposit(),
                        p.getCreatedAt() != null
                                ? p.getCreatedAt().toInstant().atZone(ZoneId.systemDefault())
                                .toLocalDate().toString()
                                : null,
                        productImageRepo.findTopImageUrlByProductId(p.getProductId())
                )).toList();
    }
}

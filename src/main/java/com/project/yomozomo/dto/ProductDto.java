package com.project.yomozomo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto {
    private int productId;
    private String title;
    private int price;
    private int deposit;
    private String createdAt;  // String으로 포맷된 날짜
    private String imageUrl;

    public ProductDto(int productId, String title, int price, int deposit, String createdAt, String imageUrl) {
        this.productId = productId;
        this.title = title;
        this.price = price;
        this.deposit = deposit;
        this.createdAt = createdAt;
        this.imageUrl = (imageUrl != null) ? imageUrl.trim() : null;
    }

    public ProductDto(int productId, String title, String imageUrl) {
        this.productId = productId;
        this.title = title;
        this.imageUrl = imageUrl;
    }

}
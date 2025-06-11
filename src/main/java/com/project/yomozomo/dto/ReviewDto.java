package com.project.yomozomo.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReviewDto {
    private Long rentalId;   // 대여 ID
    private Long targetId;   // 평점 받는 유저 ID (ex. 판매자 or 대여자)
    private Long productId;  // 상품 ID
    private BigDecimal rating; // 평점
    private String reviewText; // 리뷰 내용
    private String productTitle;
    private String targetUsername; // ← 추가!!
    private String targetName;     // ← 추가!!
    private String createdAt;
}
package com.project.yomozomo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProductDetailDto {
    private long productId;
    private String title;
    private String description;
    private int price;
    private int deposit;
    private String createdAt;
    private List<String> imageUrls;

    private String sellerNickname;
    private String sellerProfileImageUrl;
    private String sellerAddress;
    private String sellerGrade;

    private boolean wished;
    private int viewCount;
    private boolean chatExists;
}

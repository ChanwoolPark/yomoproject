package com.project.yomozomo.util;

import org.springframework.stereotype.Component;

@Component
public class StarUtil {
    public String renderStars(double rating) {
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) rating;
        boolean halfStar = (rating - fullStars) >= 0.5;

        for (int i = 0; i < fullStars; i++) stars.append("★");
        if (halfStar) stars.append("☆"); // 반개 느낌
        for (int i = fullStars + (halfStar ? 1 : 0); i < 5; i++) stars.append("✩");

        return stars.toString(); // ★★★★☆✩ 이런 식
    }
}

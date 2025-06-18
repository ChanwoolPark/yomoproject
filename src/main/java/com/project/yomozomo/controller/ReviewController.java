package com.project.yomozomo.controller;

import com.project.yomozomo.dto.ReviewDto;
import com.project.yomozomo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    // 생성자 주입!
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    @PostMapping("/reviews/write")
    @ResponseBody
    public Map<String, Object> writeReview(@RequestBody ReviewDto dto, Principal principal) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 리뷰 저장 (rentalId, reviewer, rating, content)
            reviewService.saveReview(dto, principal.getName());
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

}

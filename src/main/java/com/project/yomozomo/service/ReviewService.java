package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.dto.ReviewDto;
import com.project.yomozomo.entity.Review;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.ReviewRepository;
import com.project.yomozomo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final UserRepository userRepo;
    private final RentalRepository rentalRepo;
    private final ReviewRepository reviewRepo;
    private final ProductRepository productRepo;

    public ReviewService(UserRepository userRepo, RentalRepository rentalRepo, ReviewRepository reviewRepo,
                         ProductRepository productRepo) {
        this.userRepo = userRepo;
        this.rentalRepo = rentalRepo;
        this.reviewRepo = reviewRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public void saveReview(ReviewDto dto, String username) {
        // username(리뷰작성자)로 reviewer 엔티티 찾기
        User reviewer = userRepo.findByUsername(username).orElseThrow();
        User target = userRepo.findById(dto.getTargetId()).orElseThrow();
        Rental rental = rentalRepo.findById(dto.getRentalId()).orElseThrow();
        Product product = productRepo.findById(dto.getProductId().intValue()).orElseThrow();

        Review review = Review.builder()
                .rental(rental)
                .reviewer(reviewer)
                .target(target)
                .product(product)
                .rating(dto.getRating())
                .reviewText(dto.getReviewText())
                .build();

        reviewRepo.save(review);
        updateUserRating(target);
    }


    public void updateUserRating(User user) {
        // 이 유저(target)가 받은 모든 리뷰 조회
        List<Review> reviews = reviewRepo.findByTargetId(user.getId());

        // 평점 평균 구하기 (null safe)
        double avg = reviews.stream()
                .map(Review::getRating)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        user.setRating(BigDecimal.valueOf(avg));
        user.setReviewCount(reviews.size());
        userRepo.save(user);
    }

}

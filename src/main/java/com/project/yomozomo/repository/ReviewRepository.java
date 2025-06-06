package com.project.yomozomo.repository;

import com.project.yomozomo.entity.Review;
import com.project.yomozomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r JOIN FETCH r.reviewer WHERE r.product.productId = :productId")
    List<Review> findByProductIdWithReviewer (@Param("productId") Long productId);


    // 📌 대상 유저에 대한 평균 평점
    @Query("SELECT ROUND(AVG(r.rating), 1) FROM Review r WHERE r.target.id = :userId")
    Optional<Double> findAverageRatingByTargetId(@Param("userId") Long userId);

    // 📌 대상 유저가 받은 리뷰 개수
    int countByTarget_Id(Long userId);

    // 📌 특정 유저가 받은 모든 리뷰
    List<Review> findByTargetId(Long userId);

    // rentalId, reviewerId로 이미 리뷰 쓴 적 있는지 체크
    boolean existsByRental_RentalIdAndReviewer_Id(Long rentalId, Long reviewerId);

    // 📌 특정 유저가 작성한 리뷰
    List<Review> findByReviewer(User user);


}

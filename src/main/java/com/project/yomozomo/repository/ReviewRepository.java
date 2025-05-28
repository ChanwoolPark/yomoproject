package com.project.yomozomo.repository;

import com.project.yomozomo.entity.Review;
import com.project.yomozomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 📌 대상 유저에 대한 평균 평점
    @Query("SELECT ROUND(AVG(r.rating), 1) FROM Review r WHERE r.target.id = :userId")
    Optional<Double> findAverageRatingByTargetId(@Param("userId") Long userId);

    // 📌 대상 유저가 받은 리뷰 개수
    int countByTarget_Id(Long userId);

    // 📌 특정 유저가 받은 모든 리뷰
    List<Review> findByTargetId(Long userId);

    // 📌 특정 거래에 대해 이미 작성된 리뷰가 있는지 확인 (중복 방지)
    boolean existsByTransaction_TransactionIdAndReviewer_Id(Long transactionId, Long reviewerId);

    // 📌 특정 유저가 작성한 리뷰
    List<Review> findByReviewer(User user);
}

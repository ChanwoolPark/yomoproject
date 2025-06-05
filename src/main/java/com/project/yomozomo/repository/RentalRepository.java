package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Rental;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    // 해당 상품에 대해 예약된 일정 반환
    List<Rental> findByProduct_ProductIdAndStatusIn(int productId, List<String> status);

    // 로그인한 유저가 '대여중' 또는 '예약' 상태인 rental 조회
    List<Rental> findByUserIdAndStatusIn(Long userId, List<String> statusList);

    List<Rental> findByUserId(Long userId);


}

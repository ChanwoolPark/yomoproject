package com.project.yomozomo.repository;

import com.project.yomozomo.domain.WithdrawalRequest;
import com.project.yomozomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    // 한 유저가 본인 출금 요청을 모두 볼 때
    List<WithdrawalRequest> findByUser(User user);

    // 관리자 화면: 전체 출금 요청, 최신순
    List<WithdrawalRequest> findAllByOrderByRequestedAtDesc();
}
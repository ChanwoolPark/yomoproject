package com.project.yomozomo.repository;

import com.project.yomozomo.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    // 필요에 따라 커스텀 쿼리 추가 가능
}
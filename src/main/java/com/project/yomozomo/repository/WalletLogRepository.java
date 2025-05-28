package com.project.yomozomo.repository;

import com.project.yomozomo.domain.WalletLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletLogRepository extends JpaRepository<WalletLog, Long> {
    List<WalletLog> findTop5ByUserWalletIdOrderByCreatedAtDesc(Long userWalletId);


}
package com.project.yomozomo.service;
import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.repository.WalletLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WalletLogService {
    @Autowired
    private WalletLogRepository walletLogRepository;

    // (1) 충전/사용 로그 저장
    public void saveLog(Long walletId, String changeType, int amount) {
        WalletLog log = new WalletLog();
        log.setUserWalletId(walletId);
        log.setType(changeType); // "충전", "사용", "환불" 등
        log.setAmount(amount);
        log.setCreatedAt(new Date());
        walletLogRepository.save(log);
    }

    // (2) 최근 5개 충전(혹은 전체 로그) 조회
    public List<WalletLog> getRecentLogs(Long walletId) {
        return walletLogRepository.findTop5ByUserWalletIdOrderByCreatedAtDesc((walletId));
    }
}

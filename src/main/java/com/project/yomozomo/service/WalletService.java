package com.project.yomozomo.service;

import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.mapper.WalletLogMapper;
import com.project.yomozomo.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    @Autowired
    private WalletLogMapper walletLogMapper;
    @Autowired
    private UserWalletRepository userWalletRepository;

    // (1) 충전 내역 저장
    public void addChargeLog(Long userWalletId, int amount, String method) {
        WalletLog log = new WalletLog();
        log.setUserWalletId(userWalletId);
        log.setAmount(amount);
        log.setType("충전");
        walletLogMapper.insertLog(log);
    }

    public boolean charge(Long walletId, int amount) {
        // 1. 지갑 조회
        UserWallet wallet = userWalletRepository
                .findById(walletId).orElse(null);
        if(wallet == null) return false;

        // 2. 잔액 증가
        wallet.setBalance(wallet.getBalance() + amount);
        userWalletRepository.save(wallet);

        // 3. 로그 기록 등 추가 로직 가능
        // walletLogRepository.save(new WalletLog(...))

        return true;
    }

    // (2) 내역 조회
    public List<WalletLog> getLogs(Long userWalletId) {
        return walletLogMapper.findLogsByUserWalletId(userWalletId);
    }
}
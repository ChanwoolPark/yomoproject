package com.project.yomozomo.service;

import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.mapper.WalletLogMapper;
import com.project.yomozomo.repository.UserWalletRepository;
import com.project.yomozomo.repository.WalletLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    @Autowired
    private WalletLogMapper walletLogMapper;
    @Autowired
    private UserWalletRepository userWalletRepository;
    @Autowired
    private WalletLogRepository walletLogRepository;


    @Transactional
    public void increaseBalance(Long userId, int amount) {
        UserWallet wallet = userWalletRepository.findByUserId(userId);

        wallet.setBalance(wallet.getBalance() + amount);
        userWalletRepository.save(wallet);
    }

    // (1) 충전 내역 저장
    public void addChargeLog(Long userWalletId, int amount, String method) {
        WalletLog log = new WalletLog();
        log.setUserWalletId(userWalletId);
        log.setAmount(amount);
        log.setType("");
        walletLogMapper.insertLog(log);
    }

    @Transactional
    public boolean charge(Long walletId, int amount) {
        UserWallet wallet = userWalletRepository.findById(walletId).orElseThrow();
        wallet.setBalance(wallet.getBalance() + amount);
        userWalletRepository.save(wallet);

        WalletLog log = new WalletLog();
        log.setUserWalletId(walletId);
        log.setAmount(amount);
        log.setType("충전");
        log.setCreatedAt(new Date());
        walletLogRepository.save(log);

        return true;
    }

    // (2) 내역 조회
    public List<WalletLog> getLogs(Long userWalletId) {
        return walletLogMapper.findLogsByUserWalletId(userWalletId);
    }
}
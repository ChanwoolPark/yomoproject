package com.project.yomozomo.service;

import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.mapper.WalletLogMapper;
import com.project.yomozomo.repository.UserRepository;
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
    @Autowired
    private UserRepository userRepository;


    @Transactional
    public void increaseBalance(Long userId, int amount) {
        UserWallet wallet = userWalletRepository.findByUserId(userId);

        wallet.setBalance(wallet.getBalance() + amount);
        userWalletRepository.save(wallet);
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

        updateGradeIfNeeded(wallet.getUserId(), wallet.getBalance());

        return true;
    }

    // (2) 내역 조회
    public List<WalletLog> getLogs(Long userWalletId) {
        return walletLogMapper.findLogsByUserWalletId(userWalletId);
    }

    // 실제 등급 판별/업데이트 로직
    public void updateGradeIfNeeded(Long userId, int nowBalance) {
        User user = userRepository.findById(userId).orElseThrow();

        String newGrade = calculateGrade(nowBalance);
        if (!newGrade.equals(user.getGrade())) {
            user.setGrade(newGrade);
            userRepository.save(user);
        }
    }

    private String calculateGrade(int balance) {
        if (balance >= 200_000) return "DIAMOND";
        if (balance >= 100_000) return "PLATINUM";
        if (balance >= 50_000) return "GOLD";
        if (balance >= 10_000) return "SILVER";
        return "BRONZE";
    }

}
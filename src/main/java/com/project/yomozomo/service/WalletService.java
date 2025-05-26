package com.project.yomozomo.service;

import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.mapper.WalletLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {

    @Autowired
    private WalletLogMapper walletLogMapper;

    // (1) 충전 내역 저장
    public void addChargeLog(Long userWalletId, int amount, String method) {
        WalletLog log = new WalletLog();
        log.setUserWalletId(userWalletId);
        log.setAmount(amount);
        log.setType("충전");
        log.setMethod(method);
        walletLogMapper.insertLog(log);
    }

    // (2) 내역 조회
    public List<WalletLog> getLogs(Long userWalletId) {
        return walletLogMapper.findLogsByUserWalletId(userWalletId);
    }
}
package com.project.yomozomo.service;

import com.project.yomozomo.domain.WithdrawalRequest;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.repository.WithdrawalRequestRepository;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.repository.UserWalletRepository;
import com.project.yomozomo.service.WalletLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;
    private final WalletLogService walletLogService;

    @Transactional
    public WithdrawalRequest requestWithdrawal(Long userId, Integer amount, String bankName, String bankAccount) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저 없음"));
        UserWallet wallet = userWalletRepository.findByUserId(userId);

        if (wallet == null || wallet.getBalance() < amount) {
            throw new IllegalArgumentException("잔고 부족 또는 지갑 없음");
        }

        // (1) 잔고 차감
        wallet.setBalance(wallet.getBalance() - amount);
        userWalletRepository.save(wallet);

        // (2) 출금 요청 엔티티 생성
        WithdrawalRequest request = WithdrawalRequest.builder()
                .user(user)
                .amount(amount)
                .bankName(bankName)
                .bankAccount(bankAccount)
                .status("대기")
                .build();
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);

        // (3) 거래내역/로그에 기록
        walletLogService.saveLog(wallet.getWalletId(), "출금요청", -amount);

        return saved;
    }

    // 본인 출금 요청 내역
    public List<WithdrawalRequest> getUserRequests(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return withdrawalRequestRepository.findByUser(user);
    }

    // 관리자: 전체 출금 요청 리스트
    public List<WithdrawalRequest> getAllRequests() {
        return withdrawalRequestRepository.findAllByOrderByRequestedAtDesc();
    }

    // 출금 상태 변경(관리자 승인/거절용)
    @Transactional
    public void updateStatus(Long requestId, String status) {
        WithdrawalRequest req = withdrawalRequestRepository.findById(requestId).orElseThrow();
        req.setStatus(status);
        withdrawalRequestRepository.save(req);
    }
}
package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.repository.UserWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserWalletRepository userWalletRepository;
    @Autowired
    private WalletService walletService; // 로그 기록용

    // 결제 처리(포인트 차감/이력 기록)
    @Transactional
    public boolean processPayment(Long userId, Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElse(null);
        if (rental == null) return false;

        Product product = rental.getProduct();
        if (product == null) return false;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        // (1) 잔액 체크
        UserWallet userWallet = userWalletRepository.findByUserId(userId);
        int totalPrice = rental.getTotalPrice() + product.getDeposit();
        if (userWallet.getBalance() < totalPrice) return false;

        // (2) 포인트 차감
        userWallet.setBalance(userWallet.getBalance() - totalPrice);
        userWalletRepository.save(userWallet);

        // (3) 결제 내역 로그 기록
        walletService.addChargeLog(userWallet.getWalletId(), -totalPrice, "상품대여");

        // (4) rental 상태 변경
        rental.setStatus("대여중");
        rentalRepository.save(rental);

        // (5) (선택) 거래내역, 알림 등

        return true;
    }
}

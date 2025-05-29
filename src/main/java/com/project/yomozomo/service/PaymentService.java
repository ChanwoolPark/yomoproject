package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.entity.TransactionHistory;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.*;
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
    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;
    @Autowired
    private WalletLogService walletLogService;

    // 결제 처리(포인트 차감/이력 기록)
    @Transactional
    public boolean processPayment(Long userId, Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId).orElse(null);
        if (rental == null) return false;

        Product product = rental.getProduct();
        if (product == null) return false;

        User buyer = userRepository.findById(userId).orElse(null);
        if (buyer == null) return false;

        UserWallet buyerWallet = userWalletRepository.findByUserId(userId);
        int price = rental.getTotalPrice();
        int deposit = product.getDeposit();
        int totalPrice = price + deposit;

        if (buyerWallet.getBalance() < totalPrice) return false;

        // (1) 구매자 잔고 차감
        buyerWallet.setBalance(buyerWallet.getBalance() - totalPrice);
        userWalletRepository.save(buyerWallet);
        walletService.addChargeLog(buyerWallet.getWalletId(), -totalPrice, "상품대여(차감)");
        walletLogService.saveLog(buyerWallet.getWalletId(), "상품대여(차감)", -totalPrice);

        // (2) 판매자에게 가격 지급
        Long sellerId = product.getSeller().getId(); // 혹은 product.getSellerId();
        UserWallet sellerWallet = userWalletRepository.findByUserId(sellerId);
        sellerWallet.setBalance(sellerWallet.getBalance() + price);
        userWalletRepository.save(sellerWallet);
        walletService.addChargeLog(sellerWallet.getWalletId(), price, "상품대여(판매자 수익)");
        walletLogService.saveLog(sellerWallet.getWalletId(), "상품대여(판매자 수익)", price);

        // (3) 관리자(1번 유저)에게 보증금 입금
        Long adminId = 1L;
        UserWallet adminWallet = userWalletRepository.findByUserId(adminId);
        adminWallet.setBalance(adminWallet.getBalance() + deposit);
        userWalletRepository.save(adminWallet);
        walletService.addChargeLog(adminWallet.getWalletId(), deposit, "상품대여(보증금)");
        walletLogService.saveLog(adminWallet.getWalletId(), "상품대여(보증금)", deposit);

        // (4) rental 상태 변경
        rental.setStatus("대여중");
        rentalRepository.save(rental);

        // (5) (선택) 거래내역, 알림 등
        TransactionHistory history = TransactionHistory.builder()
                .sender(buyer)
                .receiver(product.getSeller())
                .product(product)
                .amount(price)  // 혹은 totalPrice, 입맛에 맞게
                .status("결제완료")
                .build();
        transactionHistoryRepository.save(history);


        return true;
    }
}

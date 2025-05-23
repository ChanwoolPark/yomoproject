import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.User;

/*
import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.User;
package com.project.yomozomo.service;

import com.project.yomozomo.domain.*;
import com.project.yomozomo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
public class PayService {

    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final UserWalletRepository walletRepo;
    private final RentalRepository rentalRepo;

    // 예: 관리자(플랫폼) 계정 userId
    private static final int ADMIN_ID = 1; // 실서비스에선 설정/환경변수에서 관리 추천

    public PayService(UserRepository userRepo,
                               ProductRepository productRepo,
                               UserWalletRepository walletRepo,
                               RentalRepository rentalRepo) {
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.walletRepo = walletRepo;
        this.rentalRepo = rentalRepo;
    }

    // 상품 정보 조회
    public Product getProduct(int productId) {
        return productRepo.findById(productId).orElse(null);
    }

    /**
     * 포인트 결제 및 대여 확정(포인트 이동, 렌탈 생성)
     * @param renterId 빌리는 사람
     * @param productId 상품
     * @param price 대여비
     * @param deposit 보증금
     * @param rentalStart 대여 시작일
     * @param rentalEnd 대여 종료일
     * @return 성공시 true, 실패(잔액부족 등) false
     */
    /*@Transactional
    public boolean processPayment(int renterId, int productId, int price, int deposit, String rentalStart, String rentalEnd) {
        // 1. 빌리는 사람(구매자) 잔고 조회 및 차감
        Optional<UserWallet> renterWalletOpt = walletRepo.findByUserId(renterId);
        if (renterWalletOpt.isEmpty()) return false;
        UserWallet renterWallet = renterWalletOpt.get();

        int totalCost = price + deposit;
        if (renterWallet.getBalance() < totalCost) {
            return false; // 포인트 부족
        }
        renterWallet.setBalance(renterWallet.getBalance() - totalCost);
        walletRepo.save(renterWallet);

        // 2. 상품/판매자 정보 조회
        Product product = productRepo.findById(productId).orElse(null);
        if (product == null) return false;
        User seller = product.getSeller();

        // 3. 판매자(빌려주는 사람) 잔고에 대여비만큼 추가
        Optional<UserWallet> sellerWalletOpt = walletRepo.findByUserId(seller.getUserId());
        if (sellerWalletOpt.isPresent()) {
            UserWallet sellerWallet = sellerWalletOpt.get();
            sellerWallet.setBalance(sellerWallet.getBalance() + price);
            walletRepo.save(sellerWallet);
        }

        // 4. 관리자(플랫폼) 잔고에 보증금만큼 추가
        Optional<UserWallet> adminWalletOpt = walletRepo.findByUserId(ADMIN_ID);
        if (adminWalletOpt.isPresent()) {
            UserWallet adminWallet = adminWalletOpt.get();
            adminWallet.setBalance(adminWallet.getBalance() + deposit);
            walletRepo.save(adminWallet);
        }

        // 5. Rental(대여 내역) 생성
        Rental rental = new Rental();
        rental.setProduct(product);
        rental.setRenterId(renterId);
        rental.setStartDate(java.sql.Date.valueOf(rentalStart));
        rental.setEndDate(java.sql.Date.valueOf(rentalEnd));
        rental.setPrice(price);
        rental.setDeposit(deposit);
        rental.setStatus("대여중");
        rental.setCreatedAt(new Date());
        rentalRepo.save(rental);

        // (필요하다면 WalletLog 등 거래로그도 추가)

        return true;
    }
}*/
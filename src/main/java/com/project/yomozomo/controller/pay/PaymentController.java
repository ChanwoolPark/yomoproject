package com.project.yomozomo.controller.pay;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.RentalRepository;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.repository.UserWalletRepository;
import com.project.yomozomo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class PaymentController {

    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserWalletRepository userWalletRepository;
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/payment/paysuccess")
    public String paySuccessPage() {
        // 결제 성공 안내 페이지
        return "/payment/paysuccess";
    }

    // 결제 페이지 진입
    @GetMapping("/payment/{rentalId}")
    public String paymentPage(@PathVariable Long rentalId, Principal principal, Model model) {
        System.out.println("[DEBUG] paymentPage called, rentalId = " + rentalId);
        if (principal == null) return "redirect:/login";
        System.out.println("[DEBUG] principal is null, redirect login");

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        Long userId = user.getId();
        System.out.println("[DEBUG] user loaded, userId = " + userId);


        Rental rental = rentalRepository.findById(rentalId).orElse(null);
        System.out.println("Rental not found: " + rentalId);
        if (rental == null){
            System.out.println("[DEBUG] rental not found: " + rentalId);
            return "error/404";
        }
        System.out.println("[DEBUG] rental found: " + rental.getRentalId());

        Product product = rental.getProduct();
        System.out.println("Product is null for rental: " + rentalId);
        if (product == null){
            System.out.println("[DEBUG] product not found for rental: " + rentalId);
            return "error/404";
        }
        System.out.println("[DEBUG] product found: " + product.getProductId());

        if (rental.getUser() == null) {
            System.out.println("[DEBUG] user not found for rental: " + rentalId);
            return "error/404";
        }
        System.out.println("[DEBUG] rental user found: " + rental.getUser().getId());

        // 반드시 구매자 본인만 결제페이지 진입 가능
        if (!rental.getUser().getId().equals(userId)) {
            return "error/403"; // 권한 없음 페이지
        }
        User seller = product.getSeller();
        if (seller == null) {
            System.out.println("[DEBUG] seller not found for product: " + product.getProductId());
            return "error/404";
        }
        System.out.println("[DEBUG] seller found: " + seller.getId());

        UserWallet userWallet = userWalletRepository.findByUserId(userId);
        if (userWallet == null) {
            System.out.println("[DEBUG] userWallet not found for user: " + userId);
            return "error/404";
        }
        System.out.println("[DEBUG] userWallet found: " + userWallet.getWalletId());

        model.addAttribute("user", user);
        model.addAttribute("seller", seller);
        model.addAttribute("rental", rental);
        model.addAttribute("product", product);
        model.addAttribute("userWallet", userWallet);

        // 결제 예상 금액 등
        model.addAttribute("totalPrice", rental.getTotalPrice());
        model.addAttribute("deposit", product.getDeposit());
        model.addAttribute("balance", userWallet.getBalance());

        return "payment/payment"; // resources/templates/payment/payment.html
    }

    // 결제 처리
    @PostMapping("/payment/submit")
    public String paymentSubmit(@RequestParam Long rentalId, Principal principal, Model model) {
        System.out.println("[DEBUG] paymentSubmit called, rentalId = " + rentalId);
        if (principal == null) {
            System.out.println("[DEBUG] principal is null, redirect login");
            return "redirect:/login";
        }
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        Long userId = user.getId();

        boolean success = paymentService.processPayment(userId, rentalId);
        System.out.println("[DEBUG] payment result = " + success);



        if (success) {
            return "redirect:/payment/paysuccess";
        } else {
            model.addAttribute("error", "결제 실패! 포인트가 부족하거나 시스템 오류.");
            return "payment/payment_fail";
        }
    }
    @PostMapping("/payment/complete/{rentalId}")
    public ResponseEntity<?> completeRental(@PathVariable Long rentalId, Principal principal) {


        try {
            // 로그인 확인
            if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");

            // rental 조회
            Rental rental = rentalRepository.findById(rentalId).orElse(null);
            if (rental == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("거래 없음");

            System.out.println("상태값: [" + rental.getStatus() + "]");
            System.out.println("길이: " + rental.getStatus().length());
            System.out.println("equals: " + "대여중".equals(rental.getStatus()));
            System.out.println("trim equals: " + "대여중".equals(rental.getStatus().trim()));


            // 결제 완료 여부(결제 컬럼이나 상태값으로 체크, 예: rental.getStatus().equals("대여중"))
            //if (!"대여중".equals(rental.getStatus().trim())) {
                //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("결제 완료된 거래만 완료처리 가능");
            //}

            // 현재 로그인한 유저가 판매자인지 체크
            String loginUsername = principal.getName();
            User loginUser = userRepository.findByUsername(loginUsername).orElse(null);
            if (loginUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자 없음");

            if (!rental.getProduct().getSeller().getId().equals(loginUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("거래완료는 판매자만 가능합니다. 문제가 있는 경우 고객센터나 신고하기를 이용해주세요.");
            }

            // 정상 처리 (서비스에 위임)
            paymentService.refundDepositAndCompleteRental(rentalId);
            return ResponseEntity.ok("거래가 정상적으로 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("거래 완료 처리 실패: " + e.getMessage());
        }
    }

}
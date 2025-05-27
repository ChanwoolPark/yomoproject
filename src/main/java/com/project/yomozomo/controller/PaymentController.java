package com.project.yomozomo.controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

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

    // 결제 페이지 진입
    @GetMapping("/payment/{rentalId}")
    public String paymentPage(@PathVariable Long rentalId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        Rental rental = rentalRepository.findById(rentalId).orElse(null);
        if (rental == null) return "error/404";
        Product product = rental.getProduct();
        if (product == null) return "error/404";
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "redirect:/login";
        User seller = product.getSeller();

        UserWallet userWallet = userWalletRepository.findByUserId(userId);

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
    public String paymentSubmit(@RequestParam Long rentalId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        boolean success = paymentService.processPayment(userId, rentalId);

        if (success) {
            return "redirect:/payment/success";
        } else {
            model.addAttribute("error", "결제 실패! 포인트가 부족하거나 시스템 오류.");
            return "payment/payment_fail";
        }
    }
}
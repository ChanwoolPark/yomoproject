/*package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.PayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PayController {

    private final PayService pointPaymentService;

    public PayController(PayService pointPaymentService) {
        this.pointPaymentService = pointPaymentService;
    }

    // 결제 페이지 진입 (GET)
    @GetMapping("/pay")
    public String showPayPage(@RequestParam("productId") int productId,
                              @RequestParam("rentalStart") String rentalStart,
                              @RequestParam("rentalEnd") String rentalEnd,
                              Model model) {
        // 상품, 대여기간 정보 조회해서 화면에 전달
        Product product = pointPaymentService.getProduct(productId);
        model.addAttribute("product", product);
        model.addAttribute("rentalStart", rentalStart);
        model.addAttribute("rentalEnd", rentalEnd);

        // 필요한 경우 대여 기간에 따른 가격 계산 로직 추가
        return "pay";
    }

    // 결제 처리 (POST)
    @PostMapping("/pay")
    public String processPayment(
            @RequestParam("productId") int productId,
            @RequestParam("rentalStart") String rentalStart,
            @RequestParam("rentalEnd") String rentalEnd,
            @RequestParam("price") int price,
            @RequestParam("deposit") int deposit,
            HttpSession session,
            Model model) {

        // 로그인 사용자 정보 가져오기
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            // 로그인 안 했으면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        // 결제(포인트 차감, 거래/렌탈 생성 등) 처리
        boolean success = pointPaymentService.processPayment(userId, productId, price, deposit, rentalStart, rentalEnd);

        if (success) {
            return "redirect:/pay/success"; // 결제 성공시 성공 페이지
        } else {
            model.addAttribute("error", "포인트 부족 또는 오류 발생");
            return "pay"; // 결제 실패시 다시 결제 페이지로
        }
    }

    // 결제 성공 페이지
    @GetMapping("/pay/success")
    public String paySuccess() {
        return "pay_success";
    }
}*/
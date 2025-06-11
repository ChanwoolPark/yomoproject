package com.project.yomozomo.controller;

import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.domain.WithdrawalRequest;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.UserWalletRepository;
import com.project.yomozomo.service.WithdrawalService;
import com.project.yomozomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/withdrawal")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;
    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;

    // 1. 출금 폼 페이지
    @GetMapping("/request")
    public String withdrawalForm(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElseThrow();
            UserWallet wallet = userWalletRepository.findByUserId(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("balance", wallet != null ? wallet.getBalance() : 0);
        }
        return "withdrawal/request";
    }

    // 2. 출금 요청 처리 (폼 제출)
    @PostMapping("/request")
    public String requestWithdrawal(@RequestParam Integer amount,
                                    @RequestParam String bankName,
                                    @RequestParam String bankAccount,
                                    Principal principal,
                                    Model model) {
        try {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElseThrow();
            withdrawalService.requestWithdrawal(user.getId(), amount, bankName, bankAccount);
            model.addAttribute("successMsg", "출금 요청이 정상적으로 접수되었습니다! (1~3일 내 처리 예정)");
            return "withdrawal/request_result";
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            return "withdrawal/request_result";
        }
    }


}
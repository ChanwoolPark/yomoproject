package com.project.yomozomo.controller.pay;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.domain.UserWallet;
import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.repository.UserWalletRepository;
import com.project.yomozomo.service.WalletLogService;
import com.project.yomozomo.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/mypage/yomopay")
public class YomoPayController {

    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;
    private final WalletService walletService;
    private final WalletLogService walletLogService;
    @GetMapping
    public String yomopayPage(Model model, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        UserWallet wallet = userWalletRepository.findByUserId(user.getId());

        if (wallet == null) {
            wallet = new UserWallet();
            wallet.setUserId(user.getId());
            wallet.setBalance(0); // 초기 잔액 0
            userWalletRepository.save(wallet);
        }

        int userPoint = wallet.getBalance();

        List<WalletLog> logs = walletLogService.getRecentLogs(wallet.getWalletId());

        model.addAttribute("point", userPoint);
        model.addAttribute("logs", logs);
        return "mypage/fragments/yomopay";
    }
    // 2. 충전 POST
    @PostMapping("/charge")
    public String charge(@RequestParam int amount, Principal principal, Model model, RedirectAttributes redirectAttributes) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        UserWallet wallet = userWalletRepository.findByUserId(user.getId());

        boolean result = walletService.charge(wallet.getWalletId(), amount); // 실제 충전 처리

        // ★★★ 여기서 로그 기록 추가 ★★★
        if(result) {
            // 충전 로그 기록
            walletLogService.saveLog(wallet.getWalletId(), "충전", amount);

            redirectAttributes.addFlashAttribute("success", "충전이 완료되었습니다!");
        } else {
            redirectAttributes.addFlashAttribute("error", "충전에 실패했습니다.");
        }
        return "redirect:/mypage/yomopay";
    }
}
package com.project.yomozomo.controller;

import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
public class ChargeController {

    @Autowired
    private WalletService walletService;

    // 충전 폼/내역 출력
    @GetMapping("/tcharge")
    public String chargePage(Model model, Principal principal) {
        // (예시) 로그인 사용자 정보로부터 user_wallet_id 조회 필요
        Long userWalletId = 1L; // 실제 로그인 세션에서 유저 지갑 ID 가져오기

        // 현재 포인트 조회 (user_wallet 테이블 등에서)
        int userPoint = 50000; // 실제 지갑 잔액 조회

        List<WalletLog> logs = walletService.getLogs(userWalletId);

        model.addAttribute("point", userPoint);
        model.addAttribute("logs", logs);

        return "charge";
    }

    // 충전 요청 처리 (결제 연동 예시 포함)
    @PostMapping("/charge")
    public String doCharge(@RequestParam int amount,
                           @RequestParam String payMethod,
                           Principal principal,
                           Model model) {
        Long userWalletId =1L; // 실제 로그인 세션에서 유저 지갑 ID 가져오기

        // 1. 결제 연동 (예: 카카오페이 API 호출 → 결제 성공 시 진행)
        // 성공 가정
        boolean paySuccess = true; // 실제 결제 성공여부 체크 필요

        if (paySuccess) {
            // 2. wallet_log 테이블에 충전 로그 저장
            walletService.addChargeLog(userWalletId, amount, payMethod);

            // 3. user_wallet 테이블의 포인트 증가 (update)
            // 별도 userWalletMapper.updatePoint(userWalletId, amount) 등 필요

            // 4. 모델에 최신 값 다시 담기
            int userPoint = 50000; // 지갑 잔액 조회
            List<WalletLog> logs = walletService.getLogs(userWalletId);

            model.addAttribute("point", userPoint);
            model.addAttribute("logs", logs);
            model.addAttribute("msg", "충전이 완료되었습니다!");
        } else {
            model.addAttribute("msg", "결제 실패");
        }
        return "charge";
    }
}

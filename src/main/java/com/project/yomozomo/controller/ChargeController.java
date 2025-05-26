package com.project.yomozomo.controller;

import com.project.yomozomo.domain.WalletLog;
import com.project.yomozomo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class ChargeController {

    @Autowired
    private WalletService walletService;

    // 충전 폼/내역 출력 (GET만 남기고, POST 삭제)
    @GetMapping("/charge")
    public String chargePage(Model model, Principal principal) {
        // (예시) 로그인 사용자 정보로부터 user_wallet_id 조회 필요
        Long userWalletId = 1L; // 실제 로그인 세션에서 유저 지갑 ID 가져오기

        // 현재 포인트 조회 (user_wallet 테이블 등에서)
        int userPoint = 50000; // 실제 지갑 잔액 조회

        // 충전 내역 (DB에서 가져오기)
        List<WalletLog> logs = walletService.getLogs(userWalletId);

        // 모델에 값 전달
        model.addAttribute("point", userPoint); // (DB 연동 시: 실제 포인트)
        model.addAttribute("logs", logs);       // (DB 연동 시: 실제 내역)

        return "charge";
    }

    // (POST /charge 부분은 완전히 삭제!)
}
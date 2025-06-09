package com.project.yomozomo.controller;

import com.project.yomozomo.domain.WithdrawalRequest;
import com.project.yomozomo.service.WithdrawalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final WithdrawalService withdrawalService;

    public AdminController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // 신고/문의/환불/정산 등 목록을 model에 담아서 넘김
        // 예: model.addAttribute("reports", reportService.findAll());
        return "admin/dashboard";
    }

    // 신고 관리
    @GetMapping("/reports")
    public String reportList(Model model) {
        // model.addAttribute("reports", reportService.findAll());
        return "admin/report-list";
    }

    // 문의 관리
    @GetMapping("/inquiries")
    public String inquiryList(Model model) {
        // model.addAttribute("inquiries", inquiryService.findAll());
        return "admin/inquiry-list";
    }

    // 환불/정산 관리
    @GetMapping("/settlements")
    public String settlementList(Model model) {
        // model.addAttribute("settlements", settlementService.findAll());
        return "admin/settlement-list";
    }

    // 4. 관리자: 전체 출금 요청 내역
    @GetMapping("/list")
    public String adminWithdrawalList(Model model) {
        List<WithdrawalRequest> list = withdrawalService.getAllRequests();
        model.addAttribute("requests", list);
        return "withdrawal/admin_list";
    }

    // 5. 관리자: 출금 승인/거절 처리
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam Long requestId, @RequestParam String status) {
        withdrawalService.updateStatus(requestId, status);
        return "redirect:/admin/list";
    }    // 추가: 회원 관리, 통계 등
}

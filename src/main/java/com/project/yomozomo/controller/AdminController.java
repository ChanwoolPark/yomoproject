package com.project.yomozomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

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

    // 추가: 회원 관리, 통계 등
}

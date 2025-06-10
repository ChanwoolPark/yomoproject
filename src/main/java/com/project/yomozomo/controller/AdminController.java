package com.project.yomozomo.controller;

import com.project.yomozomo.entity.Inquiry;
import com.project.yomozomo.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final InquiryRepository inquiryRepo;

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
    public String showAdminInquiryList(Model model) {
        List<Inquiry> inquiries = inquiryRepo.findAll(); // 혹은 최신순 정렬
        model.addAttribute("inquiries", inquiries);
        return "admin/inquiry-list";
    }

    @GetMapping("/inquiries/{id}")
    public String showInquiryDetail(@PathVariable Long id, Model model) {
        Inquiry inquiry = inquiryRepo.findById(id).orElseThrow();
        model.addAttribute("inquiry", inquiry);
        return "admin/inquiry-detail";
    }

    @PostMapping("/inquiries/{id}/reply")
    public String submitReply(@PathVariable Long id, @RequestParam String answer) {
        Inquiry inquiry = inquiryRepo.findById(id).orElseThrow();
        inquiry.setAnswer(answer);
        inquiry.setAnsweredAt(LocalDateTime.now());
        inquiry.setIsAnswered(true);
        inquiryRepo.save(inquiry);

        // 이메일 전송 기능은 선택 옵션 (추후 추가)
        return "redirect:/admin/inquiries";
    }


    // 환불/정산 관리
    @GetMapping("/settlements")
    public String settlementList(Model model) {
        // model.addAttribute("settlements", settlementService.findAll());
        return "admin/settlement-list";
    }
}

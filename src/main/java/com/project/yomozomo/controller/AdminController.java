package com.project.yomozomo.controller;

import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.domain.WithdrawalRequest;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.ChatroomReport;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.ChatroomReportService;
import com.project.yomozomo.service.WithdrawalService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final WithdrawalService withdrawalService;
    private final ChatroomReportService chatroomReportService;
    private final ChatService chatService;

    public AdminController(WithdrawalService withdrawalService, ChatroomReportService chatroomReportService, ChatService chatService) {
        this.withdrawalService = withdrawalService;
        this.chatroomReportService = chatroomReportService;
        this.chatService = chatService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // 신고/문의/환불/정산 등 목록을 model에 담아서 넘김
        // 예: model.addAttribute("reports", reportService.findAll());
        return "admin/dashboard";
    }

    // 1.상품 신고 관리
    @GetMapping("/reports")
    public String reportList(Model model) {
        // model.addAttribute("reports", reportService.findAll());
        return "admin/report-list";
    }

    // 2. 문의 관리 (고객센터)
    @GetMapping("/inquiries")
    public String inquiryList(Model model) {
        // model.addAttribute("inquiries", inquiryService.findAll());
        return "admin/inquiry-list";
    }

    // 3. 출금 정산
    @GetMapping("/list")
    public String adminWithdrawalList(Model model) {
        List<WithdrawalRequest> list = withdrawalService.getAllRequests();
        model.addAttribute("requests", list);
        return "withdrawal/admin_list";
    }

    // 3-1. 관리자: 출금 승인/거절 처리
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam Long requestId, @RequestParam String status) {
        withdrawalService.updateStatus(requestId, status);
        return "redirect:/admin/list";
    }    // 추가: 회원 관리, 통계 등

    // 4. 채팅방 신고 관리
    @GetMapping("/chatreport-list")
    public String chatReportList(Model model) {
        List<ChatroomReport> reportList = chatroomReportService.getAllReports();
        model.addAttribute("reportList", reportList);
        return "admin/chatreport-list";
    }

    // 4-1. 신고 상세 페이지
    @GetMapping("/report/{reportId}")
    public String reportDetail(@PathVariable Long reportId, Model model) {
        ChatroomReport report = chatroomReportService.getReport(reportId).orElseThrow();
        ChatRoom chatRoom = chatService.getChatRoomById(report.getChatRoomId());
        Rental rental = chatRoom.getRental();
        User seller = rental.getProduct().getSeller();
        User buyer = rental.getUser();

        model.addAttribute("report", report);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("rental", rental);
        model.addAttribute("seller", seller);
        model.addAttribute("buyer", buyer);

        return "admin/report-detail"; // templates/admin/report-detail.html
    }

    // 4-2. 신고 상태 변경 (처리중/완료/거절)
    @PostMapping("/report/status")
    public String updateReportStatus(@RequestParam Long reportId,
                                     @RequestParam String status) {
        chatroomReportService.updateStatus(reportId, status);
        return "redirect:/admin/report/" + reportId;
    }

    // 4-3. 보증금 정산 + 렌탈상태 변경
    @PostMapping("/report/deposit")
    @Transactional
    public String handleDepositTransfer(@RequestParam Long reportId,
                                        @RequestParam Long receiverId,
                                        @RequestParam int amount,
                                        @RequestParam String rentalStatus) {
        chatroomReportService.processDeposit(reportId, receiverId, amount, rentalStatus);
        return "redirect:/admin/report/" + reportId;
    }
}

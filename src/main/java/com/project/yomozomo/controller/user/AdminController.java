package com.project.yomozomo.controller.user;

import com.project.yomozomo.domain.Rental;
import com.project.yomozomo.domain.Report;
import com.project.yomozomo.domain.WithdrawalRequest;
import com.project.yomozomo.entity.ChatRoom;
import com.project.yomozomo.entity.ChatroomReport;
import com.project.yomozomo.entity.Inquiry;
import com.project.yomozomo.entity.Notice;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.InquiryRepository;
import com.project.yomozomo.service.InquiryService;
import com.project.yomozomo.service.NoticeService;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.ChatroomReportService;
import com.project.yomozomo.service.ReportService;
import com.project.yomozomo.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final InquiryRepository inquiryRepo;
    private final InquiryService inquiryService;
    private final NoticeService noticeService;
    private final ReportService reportService;
    private final WithdrawalService withdrawalService;
    private final ChatroomReportService chatroomReportService;
    private final ChatService chatService;


    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        return "admin/dashboard";
    }

    // 1.상품 신고 관리
    @GetMapping("/product-reports")
    public String productReportList(Model model) {
        model.addAttribute("reportList", reportService.getAllReports());
        return "admin/product_report_list";
    }

    // 1-2. 상품 신고 상세
    @GetMapping("/product-report/{reportId}")
    public String productReportDetail(@PathVariable Long reportId, Model model) {
        Report report = reportService.getReport(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 내역을 찾을 수 없습니다."));
        model.addAttribute("report", report);
        return "admin/product_report_detail"; // 상세페이지는 추가로 구성
    }
    // 상품 신고 상태 변경 (상세 페이지 폼 제출 시)
    @PostMapping("/product-report/{reportId}/status")
    public String updateProductReportStatus(
            @PathVariable Long reportId,
            @RequestParam String status) {

        reportService.updateStatus(reportId, status); // 이 서비스 메서드에서 실제 상태 변경
        return "redirect:/admin/product-report/" + reportId; // 상세로 리다이렉트
    }


    // 2. 문의 관리 (고객센터)
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
        inquiryService.answerInquiry(id, answer);
        return "redirect:/admin/inquiries";
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

    // 공지사항 목록 (관리자/유저 겸용)
    @GetMapping("/notices")
    public String adminNoticeList(Model model) {
        List<Notice> notices = noticeService.findAll();
        model.addAttribute("notices", notices);
        return "admin/notice-list"; // 관리자 전용 공지사항 목록
    }

    // 공지사항 작성 폼
    @GetMapping("/notices/new")
    public String noticeForm() {
        return "admin/notice-form";
    }

    // 공지사항 등록 처리
    @PostMapping("/notices/new")
    public String submitNotice(@RequestParam String title, @RequestParam String content) {
        noticeService.createNotice(title, content);
        return "redirect:/admin/notices";
    }

    // 공지 수정 폼
    @GetMapping("/notices/edit/{id}")
    public String editNoticeForm(@PathVariable Long id, Model model) {
        Notice notice = noticeService.findById(id);
        model.addAttribute("notice", notice);
        return "admin/notice-edit-form"; // 수정 폼 템플릿
    }

    // 공지 수정 처리
    @PostMapping("/notices/edit/{id}")
    public String editNoticeSubmit(@PathVariable Long id,
                                   @RequestParam String title,
                                   @RequestParam String content) {
        noticeService.updateNotice(id, title, content);
        return "redirect:/admin/notices";
    }

    // 공지 삭제
    @PostMapping("/notices/delete/{id}")
    public String deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return "redirect:/admin/notices";
    }

}

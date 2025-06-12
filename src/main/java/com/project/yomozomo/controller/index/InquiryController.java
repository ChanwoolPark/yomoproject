package com.project.yomozomo.controller.index;

import com.project.yomozomo.dto.InquiryForm;
import com.project.yomozomo.entity.Inquiry;
import com.project.yomozomo.entity.Notice;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.InquiryRepository;
import com.project.yomozomo.repository.UserRepository;
import com.project.yomozomo.service.MailService;
import com.project.yomozomo.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/support")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryRepository inquiryRepo;
    private final UserRepository userRepo;
    private final MailService mailService;
    private final NoticeService noticeService;

    // 문의 목록
    @GetMapping("/inquiries")
    public String showInquiryList(Model model) {
        model.addAttribute("inquiries", inquiryRepo.findAll());
        return "support"; // Thymeleaf 뷰
    }

    // 문의 작성 폼
    @GetMapping("/new")
    public String showInquiryForm(Model model) {
        model.addAttribute("inquiryForm", new InquiryForm());
        return "support/inquiry-form";
    }

    // 문의 등록 처리
    @PostMapping("/new")
    public String submitInquiry(@ModelAttribute InquiryForm form) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 로그인한 유저의 username
        User user = userRepo.findByUsername(username).orElse(null);

        Inquiry inquiry = new Inquiry();
        inquiry.setTitle(form.getTitle());
        inquiry.setContent(form.getContent());
        inquiry.setEmail(form.getEmail());
        inquiry.setCreatedAt(LocalDateTime.now());
        inquiry.setUser(user); // null 가능성 있으니 체크해도 됨

        // 메일 발송
        String userEmail = inquiry.getUser().getEmail();
        String username1 = inquiry.getUser().getNickname(); // 이름 or 닉네임
        String question = inquiry.getContent();
        String answer = inquiry.getAnswer();

        mailService.sendInquiryAnswerMail(userEmail, username1, question, answer);

        inquiryRepo.save(inquiry);
        return "redirect:/support/inquiries";
    }

    // 상세 보기
    @GetMapping("/{id}")
    public String viewInquiry(@PathVariable Long id, Model model) {
        Inquiry inquiry = inquiryRepo.findById(id).orElseThrow();
        model.addAttribute("inquiry", inquiry);
        return "support/inquiry-detail";
    }


    @GetMapping("/notice/{id}")
    public String supportNoticeDetail(@PathVariable Long id, Model model) {
        Notice notice = noticeService.findById(id);
        model.addAttribute("notice", notice);
        return "support/notice-detail"; // 공지 상세 템플릿
    }


}

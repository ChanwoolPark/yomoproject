package com.project.yomozomo.service;

import com.project.yomozomo.entity.Inquiry;
import com.project.yomozomo.repository.InquiryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepo;
    private final MailService mailService; // 메일 서비스 주입

    @Transactional
    public void answerInquiry(Long inquiryId, String answer) {
        Inquiry inquiry = inquiryRepo.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의가 없음"));

        inquiry.setAnswer(answer);
        inquiry.setAnsweredAt(LocalDateTime.now());
        inquiry.setIsAnswered(true);
        inquiryRepo.save(inquiry);

        // 메일 발송
        String userEmail = inquiry.getUser().getEmail();
        String username = inquiry.getUser().getNickname(); // or getName()
        String question = inquiry.getContent();
        mailService.sendInquiryAnswerMail(userEmail, username, question, answer);
    }
}

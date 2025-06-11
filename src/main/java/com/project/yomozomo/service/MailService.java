package com.project.yomozomo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("비밀번호 재설정 인증번호");
        message.setText("인증번호는 [" + code + "] 입니다.");
        mailSender.send(message);
    }

    public void sendInquiryAnswerMail(String toEmail, String username, String question, String answer) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[요모조모] 1:1 문의 답변 안내");
        message.setText(
                String.format("%s님, 안녕하세요!\n\n고객센터 1:1 문의에 대한 답변이 등록되었습니다.\n\n" +
                        "문의 내용: %s\n\n답변 내용: %s\n\n감사합니다 :)", username, question, answer)
        );
        mailSender.send(message);
    }
}
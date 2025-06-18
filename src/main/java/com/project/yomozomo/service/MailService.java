package com.project.yomozomo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    // 인증코드 저장 (테스트/간단용, 실서비스는 DB/Redis 추천)
    private final Map<String, String> dormantAuthCodeMap = new ConcurrentHashMap<>();

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

    // --- 추가! 휴면 해제 인증코드 발송 ---
    public String sendDormantAuthMail(String toEmail) {
        String code = generateCode();
        dormantAuthCodeMap.put(toEmail, code); // 인증코드 저장

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[요모조모] 휴면 해제 인증코드 안내");
        message.setText("요모조모 휴면 계정 해제를 위한 인증코드는 [" + code + "] 입니다.");

        mailSender.send(message);
        return code;
    }

    // 인증코드 확인
    public boolean verifyDormantCode(String email, String code) {
        String saved = dormantAuthCodeMap.get(email);
        if (saved != null && saved.equals(code)) {
            dormantAuthCodeMap.remove(email); // 한 번 인증하면 삭제!
            return true;
        }
        return false;
    }

    private String generateCode() {
        Random r = new Random();
        int code = 100_000 + r.nextInt(900_000);
        return String.valueOf(code);
    }
}

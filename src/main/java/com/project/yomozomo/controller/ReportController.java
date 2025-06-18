// ReportController.java 예시
package com.project.yomozomo.controller;

import com.project.yomozomo.dto.ReportRequestDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.ReportService;
import com.project.yomozomo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }


    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ReportRequestDto dto, Principal principal) {
        try {
            // 로그인한 사용자 정보
            String username = principal.getName();
            System.out.println("✅ username = " + username);

            // User 조회
            User reporter = userService.findByUsername(username);
            System.out.println("✅ reporter ID = " + reporter.getId());

            // DTO 확인
            System.out.println("✅ dto.productId = " + dto.getProductId());
            System.out.println("✅ dto.title = " + dto.getTitle());
            System.out.println("✅ dto.details = " + dto.getDetails());

            // 신고 저장
            reportService.saveReport(dto, reporter);
            System.out.println("✅ 신고 저장 완료");

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("❌ 오류 발생: " + e.getMessage());
            e.printStackTrace();  // 터진 에러 전체 출력
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }


    /*
    @PostMapping
    public ResponseEntity<?> submitReport(@RequestBody ReportRequestDto dto, Principal principal) {
        String username = principal.getName(); // 현재 로그인한 사용자의 username
        User reporter = userService.findByUsername(username); // username 기반으로 User 객체 조회

        reportService.saveReport(dto, reporter); // 신고 저장
        return ResponseEntity.ok().build(); // 성공 응답
    }*/
}
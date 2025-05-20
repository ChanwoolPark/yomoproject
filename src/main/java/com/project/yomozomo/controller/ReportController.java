package com.project.yomozomo.controller; // 패키지명은 프로젝트에 맞게 조정

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {


    @PostMapping("/submit-report")
    public String submitReport(@RequestParam String reportType,
                               @RequestParam String target,
                               @RequestParam String reason) {
        System.out.println("신고 접수:");
        System.out.println("유형: " + reportType);
        System.out.println("대상: " + target);
        System.out.println("사유: " + reason);

        // 여기에 신고 데이터를 데이터베이스에 저장하는 로직을 추가합니다.
        // 예: reportService.saveReport(reportType, target, reason);

        // 클라이언트 사이드에서 메시지를 띄우므로, 여기서는 단순히 리다이렉트하거나
        // 아니면 "신고가 완료되었습니다" 페이지를 별도로 만들어 반환할 수 있습니다.
        // 현재 HTML은 JS로 처리하므로, 이 메서드는 필요 없어질 수 있습니다.
        return "reportSuccess"; // 예시: 신고 완료 페이지로 이동 (별도 HTML 필요)
    }
}
package com.project.yomozomo.controller;

import com.project.yomozomo.entity.Report;
import com.project.yomozomo.repository.ReportRepository; // 이 줄을 추가
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

    @Autowired
    private ReportRepository reportRepository; // 오류 발생 라인

    @PostMapping("/submit-report")
    public String submitReport(@RequestParam String reportType,
                               @RequestParam String target,
                               @RequestParam String reason) {
        Report report = new Report(reportType, target, reason);
        reportRepository.save(report);

        System.out.println("신고 내용이 저장되었습니다.");

        return "report-submitted";
    }
}
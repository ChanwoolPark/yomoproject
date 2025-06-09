package com.project.yomozomo.service;

import com.project.yomozomo.entity.ChatroomReport;
import com.project.yomozomo.repository.ChatroomReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatroomReportService {

    private final ChatroomReportRepository chatroomReportRepository;

    public void submitReport(Long chatRoomId, Long reporterId, Long reportedId, String title, String content) {
        ChatroomReport report = new ChatroomReport();
        report.setChatRoomId(chatRoomId);
        report.setReporterId(reporterId);
        report.setReportedId(reportedId);
        report.setTitle(title);
        report.setContent(content);
        report.setCreatedAt(new Date());
        report.setStatus("처리중");
        chatroomReportRepository.save(report);
    }

    public List<ChatroomReport> getAllReports() {
        return chatroomReportRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<ChatroomReport> getReport(Long reportId) {
        return chatroomReportRepository.findById(reportId);
    }

    public void updateStatus(Long reportId, String status) {
        ChatroomReport report = chatroomReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 내역을 찾을 수 없습니다."));
        report.setStatus(status);
        chatroomReportRepository.save(report);
    }
}

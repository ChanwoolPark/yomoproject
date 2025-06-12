package com.project.yomozomo.repository;

import com.project.yomozomo.entity.ChatroomReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatroomReportRepository extends JpaRepository<ChatroomReport, Long> {
    List<ChatroomReport> findAllByOrderByCreatedAtDesc();

    List<ChatroomReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
}
package com.project.yomozomo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.yomozomo.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 필요한 추가적인 메서드 선언 가능
}
package com.project.yomozomo.repository;

import com.project.yomozomo.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // 특정 유저가 신고한 기록 조회
    List<Report> findByReporterId(Long id);

    // 특정 상품에 대한 신고 목록
    List<Report> findByProductProductId(Long productId);

    // 상태별 조회 (ex. 접수된 것만 보기)
    List<Report> findByStatus(String status);
}
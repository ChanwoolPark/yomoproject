package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Report;
import com.project.yomozomo.dto.ReportRequestDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;

    public ReportService(ReportRepository reportRepository, ProductRepository productRepository) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
    }

    // 1. 신고 저장
    public void saveReport(ReportRequestDto dto, User reporter) {
        Optional<Product> optionalProduct = productRepository.findById(dto.getProductId());
        if (optionalProduct.isEmpty()) {
            throw new IllegalArgumentException("해당 상품이 존재하지 않습니다.");
        }

        Product product = optionalProduct.get();

        Report report = Report.builder()
                .reporter(reporter)
                .product(product)
                .title(dto.getTitle())
                .details(dto.getDetails())
                .createdAt(LocalDateTime.now())
                .status("접수")
                .build();

        reportRepository.save(report);
    }

    // 2. 신고 전체 리스트 반환
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    // 3. 신고 상세 반환
    public Optional<Report> getReport(Long reportId) {
        return reportRepository.findById(reportId);
    }
}
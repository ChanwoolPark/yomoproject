package com.project.yomozomo.service;

import com.project.yomozomo.domain.Product;
import com.project.yomozomo.domain.Report;
import com.project.yomozomo.dto.ReportRequestDto;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.repository.ProductRepository;
import com.project.yomozomo.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;

    public ReportService(ReportRepository reportRepository, ProductRepository productRepository) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
    }

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
}

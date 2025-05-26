package com.project.yomozomo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq")
    @SequenceGenerator(name = "report_seq", sequenceName = "report_seq", allocationSize = 1)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Lob
    @Column(name = "details")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "status", length = 50, columnDefinition = "VARCHAR2(50) DEFAULT '접수'")
    private String status;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne
    @JoinColumn(name = "reporter_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "target_user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User targetUser;

    @ManyToOne
    @JoinColumn(name = "processed_by", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User processor;

    public Report() {
    }

    // 새로 추가된 생성자: reportType, target, reason을 인수로 받음
    public Report(String reportType, String target, String reason) {
        // 이 필드들은 현재 데이터베이스 스키마와 엔티티 필드에 직접적으로 매핑되지 않으므로,
        // 필요에 따라 이 값을 어떤 필드에 할당할지 결정해야 합니다.
        // 현재 스키마에는 'reportType'과 'target'에 해당하는 명시적인 컬럼이 없습니다.
        // 만약 이 값들이 'reason' 필드의 일부로 포함되어야 한다면, 아래 코드를 수정해야 합니다.
        // 예를 들어, details나 reason 필드에 이 정보들을 조합하여 저장할 수 있습니다.

        // 예시: reason 필드에 reportType과 target을 포함하여 저장하는 경우
        this.reason = "신고 유형: " + reportType + ", 대상: " + target + ", 상세 이유: " + reason;
        this.createdAt = LocalDateTime.now(); // 신고 생성 시 현재 시간 설정

        // reporterId, targetUserId 등은 이 생성자에서는 직접 설정하기 어려울 수 있습니다.
        // 만약 이 생성자를 호출하는 시점에 해당 ID들을 알 수 있다면, 인수에 추가하고 할당해야 합니다.
        // 그렇지 않다면, 이 생성자를 호출한 후 setter 메서드를 통해 값을 설정해야 합니다.
    }


    public Report(Long reportId, Long reporterId, Long targetUserId, String reason, String details, LocalDateTime createdAt, String status, Long processedBy, LocalDateTime processedAt) {
        this.reportId = reportId;
        this.reporterId = reporterId;
        this.targetUserId = targetUserId;
        this.reason = reason;
        this.details = details;
        this.createdAt = createdAt;
        this.status = status;
        this.processedBy = processedBy;
        this.processedAt = processedAt;
    }

    // Getter and Setter methods (생략 - 변경 없음)
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Long processedBy) {
        this.processedBy = processedBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public User getProcessor() {
        return processor;
    }

    public void setProcessor(User processor) {
        this.processor = processor;
    }
}
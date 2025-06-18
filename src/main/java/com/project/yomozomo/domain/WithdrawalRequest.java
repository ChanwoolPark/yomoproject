package com.project.yomozomo.domain;

import com.project.yomozomo.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "withdrawal_request_seq")
    @SequenceGenerator(name = "withdrawal_request_seq", sequenceName = "withdrawal_request_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    // 출금 요청자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 요청 금액
    @Column(name = "amount", nullable = false)
    private Integer amount;

    // 은행명
    @Column(name = "bank_name", length = 50, nullable = false)
    private String bankName;

    // 입금 받을 계좌번호
    @Column(name = "bank_account", length = 100, nullable = false)
    private String bankAccount;

    // 출금 상태: 대기, 완료, 거절 등
    @Column(name = "status", length = 30, nullable = false)
    private String status = "대기";

    // 요청 일시
    @Column(name = "requested_at", columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private LocalDateTime requestedAt;

    @PrePersist
    protected void onCreate() {
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "대기";
        }
    }
}
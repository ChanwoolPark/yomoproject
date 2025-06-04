package com.project.yomozomo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "wallet_log") // 실제 테이블 명
public class WalletLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wallet_log_seq_gen")
    @SequenceGenerator(name = "wallet_log_seq_gen", sequenceName = "wallet_log_seq", allocationSize = 1)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "wallet_id")
    private Long userWalletId;

    @Column(name = "change_type")
    private String type;   // '충전', '사용', '환불' 등

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}



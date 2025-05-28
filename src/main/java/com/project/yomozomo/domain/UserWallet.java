package com.project.yomozomo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "user_wallet")
public class UserWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_wallet_seq_gen")
    @SequenceGenerator(name = "user_wallet_seq_gen", sequenceName = "user_wallet_seq", allocationSize = 1)
    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "balance")
    private Integer balance;

    @Column(name = "last_update")
    private java.util.Date lastUpdate;
}

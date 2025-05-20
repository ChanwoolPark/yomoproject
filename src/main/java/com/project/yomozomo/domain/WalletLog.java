package com.project.yomozomo.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter@Setter
public class WalletLog {
    private Long logId;
    private Long userWalletId;
    private Integer amount;
    private String type;   // '충전', '사용', '환불' 등
    private String method; // '카카오페이', '토스', '카드' 등
    private Date createdAt;

}
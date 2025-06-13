// src/main/java/com/project/yomozomo/chat/model/ChatMessage.java
package com.project.yomozomo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    public enum MessageType {
        TALK,
        JOIN,
        LEAVE,
        PAYMENT_REQUEST, // 결제 요청
        PRICE_ADJUSTMENT_REQUEST, // 금액 조절 요청
        TRADE_COMPLETE_REQUEST // 거래 완료 요청
    }

    private Long roomId;
    private Long senderId;
    private String message;
    private MessageType messageType;
    private LocalDateTime sendTime; // 클라이언트에서 ISO 문자열로 보내면 스프링이 자동으로 LocalDateTime으로 변환합니다.
}
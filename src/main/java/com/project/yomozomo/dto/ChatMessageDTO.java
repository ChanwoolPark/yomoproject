// src/main/java/com/project/yomozomo/dto/ChatMessageDTO.java
package com.project.yomozomo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// java.time.LocalDateTime은 DTO에서는 String으로 받는 것이 일반적입니다.
// 클라이언트가 toISOString()으로 보내기 때문입니다.
// import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    // 이 MessageType enum은 클라이언트와 서버 DTO 간의 타입을 명확히 하기 위해 존재합니다.
    // ChatMessage 엔티티의 MessageType과 별개로 관리할 수 있습니다.
    public enum MessageType {
        TALK,
        JOIN,
        LEAVE,
        PAYMENT_REQUEST, // 결제 요청
        PRICE_ADJUSTMENT_REQUEST, // 금액 조절 요청
        TRADE_COMPLETE_REQUEST // 거래 완료 요청
    }

    private Long roomId;
    private Long senderId;     // ⭐ 클라이언트 JS와 동일하게 Long 타입의 senderId ⭐
    private String message;    // ⭐ 클라이언트 JS와 동일하게 String 타입의 message ⭐
    private MessageType messageType; // ⭐ MessageType enum 사용 ⭐
    private String sendTime;   // ⭐ 클라이언트 JS와 동일하게 String 타입의 sendTime (ISO 문자열) ⭐

    // (선택 사항) 이미지 URL 필드
    private String imgUrl;
}
// src/main/java/com/project/yomozomo/dto/ChatMessageDTO.java
package com.project.yomozomo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

// java.time.LocalDateTime은 DTO에서는 String으로 받는 것이 일반적입니다.
// 클라이언트가 toISOString()으로 보내기 때문입니다.
// import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
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
    private String senderName; // 메시지 발신자의 닉네임을 담을 필드 (이전에 추가)

    // ⭐⭐ 이 필드와 getter/setter를 추가해야 합니다. ⭐⭐
    private String imgUrl; // 이미지 URL을 담을 필드
    // Lombok의 @Getter/@Setter 어노테이션이 위에 선언되어 있으므로,
    // 별도로 getImgUrl()과 setImgUrl() 메서드를 직접 작성할 필요는 없습니다.
    // Lombok이 자동으로 생성해 줄 것입니다.

    // 만약 Lombok을 사용하지 않는다면, 아래 메서드들을 직접 추가해야 합니다.
    /*
    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
    */
}
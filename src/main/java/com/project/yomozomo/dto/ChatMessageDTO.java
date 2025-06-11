// src/main/java/com/project/yomozomo/dto/ChatMessageDTO.java
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
public class ChatMessageDTO {
    public enum MessageType {
        TALK,
        JOIN,
        LEAVE,
        IMAGE, // ⭐⭐ 'imgUrl' 대신 'IMAGE'를 추가해야 합니다. ⭐⭐
        PAYMENT_REQUEST,
        PRICE_ADJUSTMENT_REQUEST,
        TRADE_COMPLETE_REQUEST,
        TEXT,
        SYSTEM
    }

    private Long roomId;
    private Long senderId;
    private String message;
    private MessageType messageType;
    private LocalDateTime sendTime;
    private String senderName;
    private String imgUrl; // ⭐⭐ 이 필드는 Enum 밖에 올바르게 위치합니다. ⭐⭐

    // Lombok을 사용하면 @Getter/@Setter가 자동으로 이 메서드들을 생성해줍니다.
    // 명시적으로 작성할 필요는 없지만, 존재해도 문제는 없습니다.
    // public String getImgUrl() {
    //     return imgUrl;
    // }
    // public void setImgUrl(String imgUrl) {
    //     this.imgUrl = imgUrl;
    // }

}
package com.project.yomozomo.dto;

import lombok.Getter;
import lombok.Setter;

// ChatMessage DTO (데이터베이스 ID 필드 추가)
@Getter
@Setter
public class ChatMessage {
    private String webSocketRoomId; // 웹소켓 라우팅용
    // ⭐ 새로 추가된 getter/setter ⭐
    private Long dbRoomId; // ⭐ 데이터베이스 ChatRoom ID ⭐

    public void setWebSocketRoomId(String webSocketRoomId) {
        this.webSocketRoomId = webSocketRoomId;
    }

    public void setDbRoomId(Long dbRoomId) { // ⭐ 새로 추가된 getter/setter ⭐
        this.dbRoomId = dbRoomId;
    }


    public class ChatStartRequest {
        private Long rentalId; // ⭐ 이 이름을 백엔드에서 기대해야 함 ⭐

        public Long getRentalId() {
            return rentalId;
        }

        public void setRentalId(Long rentalId) {
            this.rentalId = rentalId;
        }
        // 필요하다면 private Long productId; 등 다른 필드 추가
    }
}
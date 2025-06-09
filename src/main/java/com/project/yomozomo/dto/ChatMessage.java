// ChatMessage.java (수정 후)
package com.project.yomozomo.dto;

import lombok.Getter;
import lombok.Setter;

public class ChatMessage {
    private String webSocketRoomId;
    private Long dbRoomId; // 데이터베이스 ChatRoom ID
    // 이젠 Lombok이 getter/setter를 알아서 만들어줍니다.
}
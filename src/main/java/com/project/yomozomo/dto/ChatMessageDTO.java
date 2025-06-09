// com.project.yomozomo.dto.ChatMessageDTO.java
package com.project.yomozomo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String sender;
    private String receiver;
    private String content;
    private String type;
    private String time;
    private String roomId; // 웹소켓용 문자열 ID (예: "userA_userB")
    private Long dbChatRoomId; // ⭐ 추가: 실제 DB ChatRoom ID ⭐
}
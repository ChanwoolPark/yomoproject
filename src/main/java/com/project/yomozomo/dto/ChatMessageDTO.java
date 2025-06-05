// src/main/java/com/project/yomozomo/dto/chat/ChatMessageDTO.java
package com.project.yomozomo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 이 DTO는 클라이언트에서 서버로 메시지를 보낼 때 사용됩니다.
// 클라이언트의 메시지 JSON 구조와 필드명이 일치해야 합니다.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private String sender;      // 클라이언트가 보낸 사용자 이름 (예: "userA")
    private String receiver;    // 필요한 경우
    private String content;     // 클라이언트가 보낸 메시지 내용
    private String type;        // 메시지 타입 (TALK, JOIN, LEAVE)
    private String time;        // 클라이언트에서 표시할 시간 (서버에서 설정)
    private String roomId;      // 채팅방 ID (클라이언트에서 String으로 보냄, 예: "userA_userB")

    // 참고: 만약 클라이언트가 `{"senderId": 27, "message": "Hello"}` 와 같이 보낸다면,
    // 이 DTO도 `private Long senderId; private String message;` 와 같이 변경해야 합니다.
    // 현재는 기존 코드의 필드명(sender, content)을 따릅니다.
}
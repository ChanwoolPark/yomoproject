// src/main/java/com/chat/controller/WebSocketChatController.java
package com.project.yomozomo.controller;

import com.project.yomozomo.entity.Chat;
import com.project.yomozomo.service.ChatService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class WebSocketChatController {

    private final SimpMessageSendingOperations messagingTemplate; // 메시지 전송용
    private final ChatService chatService;

    @Autowired
    public WebSocketChatController(SimpMessageSendingOperations messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    // 클라이언트가 /app/chat.sendMessage 로 메시지를 보낼 때 호출
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // TODO: 실제 유저 ID와 룸 ID를 세션에서 가져오거나, 메시지에 포함하여 사용
        // 현재는 예시용으로 고정 값 사용
        Long roomId = chatMessage.getRoomId();
        Long userId = chatMessage.getUserId(); // 메시지에 userId 포함 가정

        // 데이터베이스에 메시지 저장
        Chat savedChat = chatService.saveChatMessage(
                roomId,
                userId,
                chatMessage.getContent(),
                chatMessage.getImgUrl(),
                chatMessage.getType()
        );

        // 메시지 저장 후, 해당 채팅방을 구독하고 있는 클라이언트들에게 메시지 전송
        // /topic/public (전체 채팅) 또는 /topic/chat/{roomId} (특정 방 채팅)
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedChat);
    }

    // 클라이언트가 /app/chat.addUser 로 메시지를 보낼 때 호출 (사용자 입장 알림 등)
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // WebSocket 세션에 사용자 이름 저장 (선택 사항)
        // headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // 사용자 입장 메시지를 해당 채팅방에 전송
        Long roomId = chatMessage.getRoomId();
        String systemMessage = chatMessage.getSender() + "님이 입장했습니다.";
        // 시스템 메시지 저장 (sender: -1 or system user)
        Chat savedChat = chatService.saveChatMessage(
                roomId,
                chatMessage.getUserId(), // 입장하는 유저의 ID
                systemMessage,
                null,
                "SYSTEM"
        );
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, savedChat);
    }

    // 웹소켓을 통해 클라이언트에서 받는 메시지 DTO
    @Getter
    @Setter
    public static class ChatMessage {
        private String type; // TEXT, IMAGE, SYSTEM, MAP 등
        private Long roomId;
        private Long userId; // 메시지를 보낸 사용자 ID
        private String sender; // 메시지를 보낸 사용자 이름 (프론트엔드 표기용)
        private String content; // 메시지 내용 (텍스트)
        private String imgUrl; // 이미지 메시지인 경우 이미지 URL
        // TODO: 지도 정보 등 추가 필드
    }
}
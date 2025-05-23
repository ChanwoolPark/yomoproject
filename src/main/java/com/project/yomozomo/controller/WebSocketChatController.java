package com.project.yomozomo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model; // Model 객체를 사용하기 위해 임포트
import org.springframework.beans.factory.annotation.Value; // @Value 어노테이션을 사용하기 위해 임포트

// 예시 메시지 DTO (필요에 따라 만드세요)
class ChatMessage {
    private String sender;
    private String content;
    private String type; // "CHAT", "JOIN", "LEAVE" 등

    // Getters and Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}


@Controller
public class WebSocketChatController {

    // application.properties의 naver.map.client-id 값을 주입받습니다.
    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    // SimpMessageSendingOperations를 통해 특정 클라이언트 또는 특정 구독자에게 메시지를 보낼 수 있습니다.
    private final SimpMessageSendingOperations messagingTemplate;

    // 생성자 주입으로 SimpMessageSendingOperations 인스턴스를 주입받습니다.
    public WebSocketChatController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // HTTP 요청으로 채팅 페이지를 보여주는 메서드 (선택 사항)
    @GetMapping("/chat")
    public String chatPage(Model model) { // Model 파라미터를 추가합니다.
        // "naverMapsClientId"라는 이름으로 HTML 템플릿에 값을 전달합니다.
        model.addAttribute("naverMapsClientId", naverMapClientId);
        System.out.println("디버깅: 네이버 맵 클라이언트 ID = " + naverMapClientId); // 확인용 로그
        return "chat"; // src/main/resources/templates/chat.html 파일을 렌더링
    }


    // 클라이언트가 "/app/chat.sendMessage" 경로로 메시지를 보낼 때 이 메서드가 호출됩니다.
    // @Payload: 메시지 본문을 ChatMessage 객체로 바인딩
    // @SendTo("/topic/public"): 이 메서드의 반환 값을 "/topic/public"을 구독하는 모든 클라이언트에게 보냅니다.
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public") // 이 메서드의 결과는 이 목적지로 브로드캐스트 됩니다.
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // 메시지를 받았을 때 필요한 로직 (예: DB 저장, 로그 등)
        System.out.println("Received message: " + chatMessage.getContent() + " from " + chatMessage.getSender());
        return chatMessage; // 받은 메시지를 그대로 다시 클라이언트에게 브로드캐스트
    }

    // 클라이언트가 "/app/chat.addUser" 경로로 메시지를 보낼 때 호출됩니다.
    // 이 메서드는 @SendTo를 사용하지 않고, messagingTemplate을 사용하여 직접 메시지를 보냅니다.
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage) {
        chatMessage.setType("JOIN"); // 메시지 타입을 "JOIN"으로 설정 (예시)
        System.out.println("User joined: " + chatMessage.getSender());

        // 특정 목적지로 메시지를 전송 (이 경우에는 /topic/public을 구독하는 모든 사용자)
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
}
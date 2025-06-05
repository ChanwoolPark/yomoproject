package com.project.yomozomo.controller;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.UserService;

// ⭐ DB 저장용 ChatMessage 엔티티 임포트 ⭐
import com.project.yomozomo.entity.ChatMessage;
import com.project.yomozomo.service.ChatMessageService;

// ⭐ 새로 만든 DTO 임포트 (가장 중요!) ⭐
import com.project.yomozomo.dto.chat.ChatMessageDTO; // ⭐ 이 줄이 이름 충돌을 해결합니다 ⭐

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

// ⭐ 중요: 이전에 여기에 있던 'class ChatMessage { ... }' DTO 정의는 완전히 삭제해야 합니다! ⭐
// ⭐ 이 부분이 이름 충돌의 원인이었습니다. ⭐


@Controller
public class WebSocketChatController {

    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService;
    private final ChatMessageService chatMessageService; // ChatMessageService 주입 추가

    // 생성자에 ChatMessageService 추가
    public WebSocketChatController(SimpMessageSendingOperations messagingTemplate, UserService userService, ChatMessageService chatMessageService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/chat")
    public String chatPage(Model model, Principal principal,
                           @RequestParam(value = "targetUsername", required = false) String targetUsernameParam) {

        String currentUser = "anonymous";
        if (principal != null) {
            currentUser = principal.getName();
        }

        String finalTargetUsername;
        if (targetUsernameParam != null && !targetUsernameParam.isEmpty()) {
            finalTargetUsername = targetUsernameParam;
        } else {
            System.out.println("디버깅: targetUsername 파라미터 없음. 기본 상대방 설정.");
            User loggedInUser = userService.findByUsername(currentUser);
            if (loggedInUser != null) {
                if ("userA".equals(currentUser)) {
                    finalTargetUsername = "userB";
                } else if ("userB".equals(currentUser)) {
                    finalTargetUsername = "userA";
                } else {
                    finalTargetUsername = "default_target";
                }
            } else {
                finalTargetUsername = "default_target";
            }
        }

        User targetUserEntity = userService.findByUsername(finalTargetUsername);
        if (targetUserEntity == null) {
            System.err.println("Error: Target user '" + finalTargetUsername + "' not found in DB.");
            return "error_page";
        }

        model.addAttribute("username", currentUser);
        model.addAttribute("targetUsername", finalTargetUsername);
        model.addAttribute("naverMapsClientId", naverMapClientId);

        Set<String> users = new TreeSet<>(Comparator.naturalOrder());
        users.add(currentUser);
        users.add(finalTargetUsername);
        String roomId = String.join("_", users);

        model.addAttribute("roomId", roomId);

        System.out.println("디버깅: 현재 사용자 = " + currentUser + ", 상대방 = " + finalTargetUsername + ", Room ID = " + roomId);
        return "chat";
    }

    @MessageMapping("/chat.sendMessage") // 클라이언트에서 /pub/chat.sendMessage 로 메시지를 보냅니다.
    public void sendMessage(@Payload ChatMessageDTO chatMessageDTO) { // ⭐ DTO 타입 변경: ChatMessageDTO 사용 ⭐
        // 클라이언트가 보낸 시간은 무시하고, 서버에서 현재 시간으로 설정
        LocalDateTime sendTime = LocalDateTime.now();
        chatMessageDTO.setTime(sendTime.format(DateTimeFormatter.ofPattern("HH:mm"))); // 클라이언트에 표시할 시간 설정

        if (chatMessageDTO.getRoomId() == null || chatMessageDTO.getRoomId().isEmpty()) {
            System.err.println("Error: Room ID is missing for chat message in payload!");
            return;
        }

        // ⭐ 1. DTO를 엔티티로 변환하여 DB에 저장 ⭐
        com.project.yomozomo.entity.ChatMessage dbChatMessage = new com.project.yomozomo.entity.ChatMessage();

        // roomId (String)를 Long으로 변환 (DB에 Long으로 저장하기 위함)
        Long chatRoomIdLong = null;
        try {
            chatRoomIdLong = Long.parseLong(chatMessageDTO.getRoomId());
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid Room ID format from payload: " + chatMessageDTO.getRoomId());
            return;
        }
        dbChatMessage.setRoomId(chatRoomIdLong);

        // sender (String, 사용자 이름/닉네임)를 senderId (Long, 사용자 ID)로 변환
        User senderUser = userService.findByUsername(chatMessageDTO.getSender());
        if (senderUser != null) {
            dbChatMessage.setSenderId(senderUser.getId());
        } else {
            System.err.println("Error: Sender user not found for username: " + chatMessageDTO.getSender());
            // 적절한 에러 처리 또는 기본 사용자 ID 설정
            return;
        }

        dbChatMessage.setMessage(chatMessageDTO.getContent()); // DTO의 content를 엔티티의 message로 설정
        dbChatMessage.setSendTime(sendTime); // 서버 시간으로 설정
        dbChatMessage.setMessageType(chatMessageDTO.getType() != null ? chatMessageDTO.getType() : "TALK"); // DTO의 type을 엔티티의 messageType으로 설정

        // DB 저장 시도
        com.project.yomozomo.entity.ChatMessage savedMessage = null;
        try {
            savedMessage = chatMessageService.saveChatMessage(dbChatMessage);
            System.out.println("메시지 DB 저장 완료: " + savedMessage.getMessageId());
        } catch (Exception e) {
            System.err.println("메시지 DB 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return; // 저장 실패 시 브로드캐스트하지 않음
        }

        // ⭐ 2. 클라이언트로 메시지 브로드캐스트 ⭐
        // 저장된 메시지 엔티티(savedMessage)의 정보를 클라이언트가 받을 DTO 형태로 변환하여 보낼 수도 있습니다.
        // 여기서는 이미 ChatMessageDTO에 필요한 정보가 있으므로 DTO를 다시 사용합니다.
        messagingTemplate.convertAndSend("/topic/chat/room/" + chatMessageDTO.getRoomId(), chatMessageDTO);
        System.out.println("메시지 브로드캐스트 완료: Sender=" + chatMessageDTO.getSender() + ", Room=" + chatMessageDTO.getRoomId() + ", Content=" + chatMessageDTO.getContent());
    }

    @MessageMapping("/chat.addUser") // 클라이언트에서 /pub/chat.addUser 로 메시지를 보냅니다.
    public void addUser(@Payload ChatMessageDTO chatMessageDTO, SimpMessageHeaderAccessor headerAccessor) { // ⭐ DTO 타입 변경: ChatMessageDTO 사용 ⭐
        chatMessageDTO.setType("JOIN");
        chatMessageDTO.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        String roomId = chatMessageDTO.getRoomId();
        if (roomId == null || roomId.isEmpty()) {
            System.err.println("Error: Room ID is missing for add user message!");
            return;
        }
        // 입장 메시지도 DB에 저장하고 싶다면, sendMessage 메서드와 유사하게 변환 및 저장 로직 추가
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, chatMessageDTO);
        System.out.println("사용자 입장: User=" + chatMessageDTO.getSender() + ", Type=" + chatMessageDTO.getType() + ", Room=" + roomId);
    }
}
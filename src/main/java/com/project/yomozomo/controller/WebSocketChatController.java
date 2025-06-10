package com.project.yomozomo.controller;

import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.UserService;

// ⭐ DB 저장용 ChatMessage 엔티티 임포트 ⭐
import com.project.yomozomo.entity.ChatMessage;
import com.project.yomozomo.service.ChatMessageService;

// ⭐ 새로 만든 DTO 임포트 (가장 중요!) ⭐
import com.project.yomozomo.dto.ChatMessageDTO;

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

@Controller
public class WebSocketChatController {

    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService;
    private final ChatMessageService chatMessageService;

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
                if ("userA".equals(currentUser)) { // 예시: userA 로그인 시 userB와 채팅
                    finalTargetUsername = "userB";
                } else if ("userB".equals(currentUser)) { // 예시: userB 로그인 시 userA와 채팅
                    finalTargetUsername = "userA";
                } else {
                    finalTargetUsername = "default_target"; // 그 외 로그인 사용자
                }
            } else {
                finalTargetUsername = "default_target"; // 로그인하지 않은 사용자
            }
        }

        User targetUserEntity = userService.findByUsername(finalTargetUsername);
        if (targetUserEntity == null) {
            System.err.println("Error: Target user '" + finalTargetUsername + "' not found in DB.");
            return "error_page"; // 적절한 에러 페이지로 리다이렉트
        }

        model.addAttribute("username", currentUser);
        model.addAttribute("targetUsername", finalTargetUsername);
        model.addAttribute("naverMapsClientId", naverMapClientId);

        // 채팅방 ID 생성 로직 (두 사용자 이름을 기반으로 고유한 ID 생성)
        Set<String> users = new TreeSet<>(Comparator.naturalOrder()); // TreeSet을 사용해 사용자 이름 순서 보장
        users.add(currentUser);
        users.add(finalTargetUsername);
        String roomId = String.join("_", users); // 예: "userA_userB"

        model.addAttribute("roomId", roomId);

        System.out.println("디버깅: 현재 사용자 = " + currentUser + ", 상대방 = " + finalTargetUsername + ", Room ID = " + roomId);
        return "chat"; // chat.html 템플릿 반환
    }

    @MessageMapping("/chat.sendMessage") // 클라이언트에서 /pub/chat.sendMessage 로 메시지를 보냅니다.
    public void sendMessage(@Payload ChatMessageDTO chatMessageDTO) {
        // 클라이언트가 보낸 시간은 무시하고, 서버에서 현재 시간으로 설정 (DB 저장용)
        LocalDateTime dbSendTime = LocalDateTime.now();
        // 클라이언트에 표시할 시간 (HH:mm)은 DTO의 sendTime 필드에 설정
        chatMessageDTO.setSendTime(dbSendTime.format(DateTimeFormatter.ofPattern("HH:mm")));

        if (chatMessageDTO.getRoomId() == null) { // Long 타입은 null 체크만 필요
            System.err.println("Error: Room ID is missing for chat message in payload!");
            return;
        }

        // ⭐ 1. DTO를 엔티티로 변환하여 DB에 저장 ⭐
        com.project.yomozomo.entity.ChatMessage dbChatMessage = new com.project.yomozomo.entity.ChatMessage();

        // roomId (Long)는 이미 DTO에 Long 타입으로 있으므로 바로 사용
        dbChatMessage.setRoomId(chatMessageDTO.getRoomId());

        // senderId (Long)를 사용하여 User 엔티티 조회
        User senderUser = null;
        try {
            // ⭐ getUserById 메서드 사용 ⭐
            senderUser = userService.getUserById(chatMessageDTO.getSenderId());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: Sender user not found for ID: " + chatMessageDTO.getSenderId() + " - " + e.getMessage());
            return; // 사용자를 찾을 수 없으므로 저장 및 브로드캐스트하지 않음
        }

        if (senderUser != null) {
            dbChatMessage.setSenderId(senderUser.getId());
        } else {
            // 이 부분은 위의 try-catch에서 이미 처리될 가능성이 높지만, 방어적으로 남겨둘 수 있습니다.
            System.err.println("Error: Sender user not found for ID (after getUserById check): " + chatMessageDTO.getSenderId());
            return;
        }

        dbChatMessage.setMessage(chatMessageDTO.getMessage()); // DTO의 message를 엔티티의 message로 설정
        dbChatMessage.setSendTime(dbSendTime); // 서버 시간 (LocalDateTime)으로 설정
        // DTO의 MessageType enum을 엔티티의 String messageType으로 변환
        if (chatMessageDTO.getMessageType() != null) {
            dbChatMessage.setMessageType(chatMessageDTO.getMessageType().name());
        } else {
            dbChatMessage.setMessageType(ChatMessageDTO.MessageType.TALK.name()); // 기본값 설정
        }
        // imgUrl 필드가 ChatMessageDTO에 있다면 (DTO에 getImgUrl() 메서드가 있다고 가정)
        // dbChatMessage.setImgUrl(chatMessageDTO.getImgUrl());


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
        // 클라이언트에게 브로드캐스트할 때는 이미 적절히 설정된 chatMessageDTO를 그대로 보냅니다.
        // 클라이언트 구독 경로: /topic/chat/room/{roomId}
        messagingTemplate.convertAndSend("/topic/chat/room/" + chatMessageDTO.getRoomId(), chatMessageDTO);
        System.out.println("메시지 브로드캐스트 완료: Sender=" + chatMessageDTO.getSenderId() + ", Room=" + chatMessageDTO.getRoomId() + ", Message=" + chatMessageDTO.getMessage());
    }

    @MessageMapping("/chat.addUser") // 클라이언트에서 /pub/chat.addUser 로 메시지를 보냅니다.
    public void addUser(@Payload ChatMessageDTO chatMessageDTO, SimpMessageHeaderAccessor headerAccessor) {
        // 입장 메시지 타입 설정
        chatMessageDTO.setMessageType(ChatMessageDTO.MessageType.JOIN);
        // 입장 시간 설정 (클라이언트에 표시할 HH:mm 형식)
        chatMessageDTO.setSendTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        // roomId 유효성 검사
        if (chatMessageDTO.getRoomId() == null) { // Long 타입은 null 체크만 필요
            System.err.println("Error: Room ID is missing for add user message!");
            return;
        }
        String roomId = String.valueOf(chatMessageDTO.getRoomId()); // 브로드캐스트 경로를 위해 String으로 변환

        // (선택 사항) 입장 메시지도 DB에 저장하고 싶다면, sendMessage 메서드와 유사하게 변환 및 저장 로직 추가
        /*
        com.project.yomozomo.entity.ChatMessage dbChatMessage = new com.project.yomozomo.entity.ChatMessage();
        dbChatMessage.setRoomId(chatMessageDTO.getRoomId());
        dbChatMessage.setSenderId(chatMessageDTO.getSenderId()); // Long ID 사용
        dbChatMessage.setMessage(chatMessageDTO.getMessage()); // 클라이언트에서 보낸 메시지 ('OOO님이 입장했습니다.')
        dbChatMessage.setMessageType(ChatMessageDTO.MessageType.JOIN.name());
        dbChatMessage.setSendTime(LocalDateTime.now());
        try {
            chatMessageService.saveChatMessage(dbChatMessage);
            System.out.println("입장 메시지 DB 저장 완료.");
        } catch (Exception e) {
            System.err.println("입장 메시지 DB 저장 중 오류 발생: " + e.getMessage());
        }
        */

        // 클라이언트에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, chatMessageDTO);
        System.out.println("사용자 입장: User=" + chatMessageDTO.getSenderId() + ", Type=" + chatMessageDTO.getMessageType() + ", Room=" + roomId);
    }
}
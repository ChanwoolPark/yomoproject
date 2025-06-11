package com.project.yomozomo.controller.chat;

import com.project.yomozomo.entity.User; // User 엔티티/DTO 임포트
import com.project.yomozomo.service.UserService; // UserService 임포트

// 또는 java.security.Principal; (Spring Security 사용하는 경우)
import java.security.Principal; // Principal 임포트

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; // @RequestParam 임포트
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

// ChatMessage DTO (동일)
class ChatMessage {
    private String sender;
    private String receiver;
    private String content;
    private String type;
    private String time;
    private String roomId;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}


@Controller
public class WebSocketChatController {

    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService; // UserService 주입

    // 생성자에 UserService 추가
    public WebSocketChatController(SimpMessageSendingOperations messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService; // 주입받은 UserService 저장
    }

    @GetMapping("/chat")
    // @RequestParam으로 targetUsername을 받습니다.
    // Principal 또는 HttpSession으로 currentUser를 가져옵니다.
    public String chatPage(Model model, Principal principal,
                           @RequestParam(value = "targetUsername", required = false) String targetUsernameParam) { // required=false로 선택적 파라미터로 만듦

        // 1. 현재 로그인한 사용자 정보 가져오기 (Spring Security 사용하는 경우)
        String currentUser = "anonymous"; // 기본값 설정

        if (principal != null) {
            // Principal 객체에서 사용자 이름을 가져옵니다.
            // Spring Security를 사용하면 Principal.getName()이 일반적으로 username을 반환합니다.
            currentUser = principal.getName();

            // 만약 사용자 정보를 더 상세하게 관리하는 Custom UserDetails를 사용한다면:
            // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            //     CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            //     currentUser = userDetails.getUsername(); // 또는 userDetails.getNickname() 등
            // }
        }

        // 2. 상대방 사용자 정보 결정
        String finalTargetUsername;
        if (targetUsernameParam != null && !targetUsernameParam.isEmpty()) {
            // URL 파라미터로 상대방이 명시된 경우
            finalTargetUsername = targetUsernameParam;
        } else {
            // URL 파라미터가 없는 경우 (예: /chat으로 직접 접근)
            // 실제 애플리케이션에서는 사용자 목록 페이지로 리다이렉트하거나
            // 기본 대화 상대를 설정해야 합니다.
            System.out.println("디버깅: targetUsername 파라미터 없음. 기본 상대방 설정.");
            // 예시: 로그인된 사용자가 'userA'면 'userB', 'userB'면 'userA'로 설정
            User loggedInUser = userService.findByUsername(currentUser);
            if (loggedInUser != null) {
                // 현재 로그인한 사용자의 친구 목록이나 최근 대화 상대 등을 조회하여
                // 기본 대화 상대를 설정하는 로직을 여기에 추가해야 합니다.
                // 예시로, 간단하게 'userA'와 'userB' 간의 대화만 지원한다고 가정합니다.
                if ("userA".equals(currentUser)) {
                    finalTargetUsername = "userB";
                } else if ("userB".equals(currentUser)) {
                    finalTargetUsername = "userA";
                } else {
                    finalTargetUsername = "default_target"; // 기본 대상 설정
                }
            } else {
                finalTargetUsername = "default_target"; // 로그인되지 않은 경우 기본 대상
            }
        }

        // 3. (선택 사항) targetUsername이 실제 DB에 존재하는 유효한 사용자인지 확인
        User targetUserEntity = userService.findByUsername(finalTargetUsername);
        if (targetUserEntity == null) {
            // 유효하지 않은 상대방이라면 에러 처리 또는 리다이렉트
            System.err.println("Error: Target user '" + finalTargetUsername + "' not found in DB.");
            return "error_page"; // 또는 "redirect:/users" (사용자 목록으로)
        }


        model.addAttribute("username", currentUser);
        model.addAttribute("targetUsername", finalTargetUsername);
        model.addAttribute("naverMapsClientId", naverMapClientId);

        // 두 사용자 이름으로 고유한 Room ID 생성 (정렬하여 항상 동일한 ID가 나오도록)
        Set<String> users = new TreeSet<>(Comparator.naturalOrder());
        users.add(currentUser);
        users.add(finalTargetUsername);
        String roomId = String.join("_", users);

        model.addAttribute("roomId", roomId);

        System.out.println("디버깅: 현재 사용자 = " + currentUser + ", 상대방 = " + finalTargetUsername + ", Room ID = " + roomId);
        return "chat";
    }

    // ... (sendMessage, addUser 메서드는 이전과 동일) ...
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (chatMessage.getRoomId() == null || chatMessage.getRoomId().isEmpty()) {
            System.err.println("Error: Room ID is missing for chat message!");
            return;
        }
        messagingTemplate.convertAndSend("/topic/chat/room/" + chatMessage.getRoomId(), chatMessage);
        System.out.println("메시지 전송: Sender=" + chatMessage.getSender() + ", Receiver=" + chatMessage.getReceiver() + ", Room=" + chatMessage.getRoomId() + ", Content=" + chatMessage.getContent());
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        chatMessage.setType("JOIN");
        chatMessage.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        String roomId = chatMessage.getRoomId();
        if (roomId == null || roomId.isEmpty()) {
            System.err.println("Error: Room ID is missing for add user message!");
            return;
        }
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, chatMessage);
        System.out.println("사용자 입장: User=" + chatMessage.getSender() + ", Type=" + chatMessage.getType() + ", Room=" + roomId);
    }

}
package com.project.yomozomo.controller; // 적절한 패키지 경로로 변경해주세요. (예: com.project.yomozomo.controller.chat)

import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.UserService;

import java.security.Principal; // Spring Security 사용하는 경우

import org.springframework.beans.factory.annotation.Value; // @Value 임포트
import org.springframework.messaging.handler.annotation.DestinationVariable; // DestinationVariable 임포트
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor; // SimpMessageHeaderAccessor 임포트
import org.springframework.messaging.simp.SimpMessageSendingOperations; // SimpMessageSendingOperations 임포트
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; // @RequestParam 임포트

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

// Lombok 어노테이션 (필요시 추가)
// import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 임포트

// ChatMessage DTO/Entity는 프로젝트의 실제 클래스(ChatMessageDTO 또는 ChatMessage)를 사용해야 합니다.
// 여기서는 예시로 제공된 ChatMessage 클래스를 사용합니다.
// 실제 프로젝트에서는 com.project.yomozomo.dto.ChatMessageDTO 또는 entity 패키지의 ChatMessage를 사용하세요.
// ChatMessageDTO가 있다면, 해당 클래스를 여기에 맞게 조정하거나 사용해주세요.
// 예를 들어, 아래 class ChatMessage 대신 import com.project.yomozomo.dto.ChatMessageDTO;를 사용하고
// 코드 내 모든 ChatMessage를 ChatMessageDTO로 변경해야 합니다.
class ChatMessage {
    private String sender;
    private String receiver;
    private String content;
    private String type; // "TALK", "JOIN", "LEAVE", "IMAGE" 등
    private String time;
    private String roomId;
    private String imgUrl; // 이미지 메시지용 필드 추가 (필요시)

    // Lombok @Data 또는 @Getter, @Setter 사용 시 아래 getter/setter 필요 없음
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

    // DTO의 MessageType enum이 있는 경우를 대비한 변환 로직 (필요시 추가)
    // 예를 들어, ChatMessageDTO.MessageType.TALK.name()으로 변환하여 사용
}

@Controller
@Slf4j // Lombok의 Slf4j 로거 사용
public class WebSocketChatController {

    // application.properties 또는 application.yml에서 주입받을 네이버 지도 클라이언트 ID
    @Value("${naver.map.client-id}")
    private String naverMapClientId;

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService; // UserService 주입

    // 생성자 주입
    public WebSocketChatController(SimpMessageSendingOperations messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    // ====================================================================================
    // ⭐⭐ 웹 페이지 (GetMapping) 로직 ⭐⭐
    // ====================================================================================

    /**
     * 채팅 페이지를 로드하고, 현재 사용자 및 대상 사용자 정보를 모델에 추가합니다.
     * 두 사용자 이름을 기반으로 고유한 채팅방 ID를 생성합니다.
     *
     * @param model 뷰에 데이터를 전달하기 위한 Model 객체
     * @param principal 현재 로그인된 사용자 정보 (Spring Security 사용 시)
     * @param targetUsernameParam URL 쿼리 파라미터로 전달된 대상 사용자 이름 (선택적)
     * @return 채팅 페이지 템플릿의 이름 또는 에러 페이지 URL
     */
    @GetMapping("/chat")
    public String chatPage(Model model, Principal principal,
                           @RequestParam(value = "targetUsername", required = false) String targetUsernameParam) {

        // 1. 현재 로그인한 사용자 정보 가져오기
        String currentUsername = "anonymous"; // 기본값 설정 (로그인되지 않은 경우)

        if (principal != null) {
            currentUsername = principal.getName(); // Spring Security Principal에서 사용자 이름 가져오기
        }

        // 2. 상대방 사용자 정보 결정
        String finalTargetUsername;
        if (targetUsernameParam != null && !targetUsernameParam.isEmpty()) {
            finalTargetUsername = targetUsernameParam; // URL 파라미터가 있으면 사용
        } else {
            // URL 파라미터가 없는 경우 (예: /chat으로 직접 접근)
            // 실제 애플리케이션에서는 사용자 목록 페이지로 리다이렉트하거나
            // 기본 대화 상대를 설정하는 로직이 필요합니다.
            log.info("targetUsername 파라미터 없음. 기본 상대방 설정 시도. 현재 사용자: {}", currentUsername);

            // 예시: 로그인된 사용자가 'userA'면 'userB'와 대화하도록 설정
            // 이 부분은 실제 서비스 로직에 따라 구현해야 합니다. (예: 최근 대화 상대, 친구 목록 등)
            if ("userA".equals(currentUsername)) {
                finalTargetUsername = "userB";
            } else if ("userB".equals(currentUsername)) {
                finalTargetUsername = "userA";
            } else {
                // 로그인된 사용자도 아니고, targetUsername도 없으면 기본 대상 설정 (에러 처리 또는 목록으로 리다이렉션 고려)
                finalTargetUsername = "default_chat_partner";
                log.warn("기본 대화 상대를 'default_chat_partner'로 설정했습니다. 실제 서비스에서는 유효한 상대가 필요합니다.");
            }
        }

        // 3. (선택 사항) finalTargetUsername이 실제 DB에 존재하는 유효한 사용자인지 확인
        // 이 부분은 UserService에 findByUsername(String username) 메서드가 있다고 가정합니다.
        User targetUserEntity = userService.findByUsername(finalTargetUsername);
        if (targetUserEntity == null) {
            log.error("대상 사용자 '{}'를 DB에서 찾을 수 없습니다. 채팅 페이지 로드 실패.", finalTargetUsername);
            model.addAttribute("errorMessage", "채팅할 상대를 찾을 수 없습니다.");
            return "error_page"; // 또는 "redirect:/users" (사용자 목록으로 리다이렉트)
        }

        // 4. Model에 데이터 추가
        model.addAttribute("username", currentUsername);
        model.addAttribute("targetUsername", finalTargetUsername);
        model.addAttribute("naverMapsClientId", naverMapClientId);

        // 5. 두 사용자 이름으로 고유한 Room ID 생성 (정렬하여 항상 동일한 ID가 나오도록)
        Set<String> users = new TreeSet<>(Comparator.naturalOrder());
        users.add(currentUsername);
        users.add(finalTargetUsername);
        String roomId = String.join("_", users); // 예: "userA_userB" 또는 "userB_userA" -> "userA_userB"

        model.addAttribute("roomId", roomId);

        log.info("채팅 페이지 로드: 현재 사용자 = {}, 상대방 = {}, Room ID = {}", currentUsername, finalTargetUsername, roomId);
        return "chat"; // 'chat.html' 템플릿 반환
    }

    // ====================================================================================
    // ⭐⭐ 웹소켓 메시지 처리 로직 (@MessageMapping) ⭐⭐
    // ====================================================================================

    /**
     * 클라이언트가 `/app/chat.sendMessage` 경로로 메시지를 보낼 때 처리합니다.
     * 메시지에 현재 시간 정보를 추가하고 해당 채팅방을 구독하는 모든 클라이언트에게 브로드캐스트합니다.
     *
     * @param chatMessage 클라이언트로부터 받은 ChatMessage 객체 (DTO)
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        // 메시지 전송 시간 설정 (서버 시간 기준)
        chatMessage.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        if (chatMessage.getRoomId() == null || chatMessage.getRoomId().isEmpty()) {
            log.error("메시지 전송 실패: Room ID가 누락되었습니다. 발신자: {}, 내용: {}", chatMessage.getSender(), chatMessage.getContent());
            return; // Room ID가 없으면 메시지 처리 중단
        }

        // 특정 토픽 경로로 메시지를 전송 (브로드캐스트)
        // 클라이언트는 /topic/chat/room/{roomId}를 구독해야 합니다.
        String destination = "/topic/chat/room/" + chatMessage.getRoomId();
        messagingTemplate.convertAndSend(destination, chatMessage);

        log.info("메시지 브로드캐스트 완료: [룸ID: {}], 발신자: {}, 내용: {}",
                chatMessage.getRoomId(), chatMessage.getSender(), chatMessage.getContent());
    }

    /**
     * 클라이언트가 `/app/chat.addUser` 경로로 입장 메시지를 보낼 때 처리합니다.
     * 사용자 입장 메시지를 해당 채팅방을 구독하는 모든 클라이언트에게 브로드캐스트합니다.
     *
     * @param chatMessage 입장 메시지 (ChatMessage 객체)
     * @param headerAccessor STOMP 헤더 정보에 접근하기 위한 객체 (세션 ID 등을 얻을 수 있음)
     */
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // 메시지 타입을 'JOIN'으로 설정
        chatMessage.setType("JOIN");
        // 메시지 전송 시간 설정
        chatMessage.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        String roomId = chatMessage.getRoomId();
        if (roomId == null || roomId.isEmpty()) {
            log.error("사용자 입장 처리 실패: Room ID가 누락되었습니다. 발신자: {}", chatMessage.getSender());
            return; // Room ID가 없으면 처리 중단
        }

        // STOMP 세션 속성에 사용자 이름과 Room ID 저장 (선택 사항, 필요에 따라)
        // headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        // headerAccessor.getSessionAttributes().put("roomId", roomId);

        // 특정 토픽 경로로 메시지를 전송 (브로드캐스트)
        String destination = "/topic/chat/room/" + roomId;
        messagingTemplate.convertAndSend(destination, chatMessage);

        log.info("사용자 입장 메시지 브로드캐스트 완료: [룸ID: {}], 사용자: {}", roomId, chatMessage.getSender());
    }
}
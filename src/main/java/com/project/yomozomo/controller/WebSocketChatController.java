package com.project.yomozomo.controller;

import com.project.yomozomo.dto.ChatMessageDTO;
import com.project.yomozomo.entity.User;
import com.project.yomozomo.service.ChatService;
import com.project.yomozomo.service.UserService;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.stereotype.Controller;
// HTTP 관련 임포트 제거
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import com.project.yomozomo.entity.ChatMessage; // 엔티티 직접 사용 안 할 경우 제거
// import java.security.Principal; // 웹소켓 메시지에 Principal 필요 없으면 제거
import java.time.LocalDateTime; // LocalDateTime 사용 시

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
// ⭐ @RequestMapping 제거: 이 컨트롤러는 HTTP 요청을 처리하지 않고 웹소켓 메시지만 처리합니다.
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate; // 웹소켓 메시지 전송용

    // ====================================================================================
    // ⭐⭐ 웹소켓 메시지 처리 로직 ⭐⭐
    // ====================================================================================

    /**
     * 클라이언트에서 보낸 STOMP 메시지를 받아서 처리하는 메서드.
     * 클라이언트가 `/pub/chat.sendMessage/{chatRoomId}` 경로로 메시지를 보낼 때 호출됩니다.
     * (이전 ChatController의 sendMessage 로직을 그대로 가져옴)
     *
     * @param chatMessageDto 클라이언트로부터 받은 ChatMessageDTO 객체
     * @param chatRoomId 메시지가 전송된 채팅방 ID (경로 변수에서 추출)
     */
    @MessageMapping("/pub/chat.sendMessage/{chatRoomId}") // ⭐ ChatController와 동일한 경로를 사용.
    // 이제 ChatController에 이 매핑이 없으므로 충돌 없음.
    public void sendMessage(@Payload ChatMessageDTO chatMessageDto,
                            @DestinationVariable Long chatRoomId) {

        log.info("WebSocketChatController에서 메시지 수신 - 룸ID(URL): {}, DTO RoomID: {}, 발신자(DTO ID): {}, 메시지(DTO): {}, 타입(DTO): {}, 전송 시간(DTO): {}",
                chatRoomId, chatMessageDto.getRoomId(), chatMessageDto.getSenderId(),
                chatMessageDto.getMessage(), chatMessageDto.getMessageType(), chatMessageDto.getSendTime());

        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            log.warn("경고: DTO의 chatRoomId({})와 URL의 chatRoomId({})가 일치하지 않습니다. URL의 ID를 사용합니다.",
                    chatMessageDto.getRoomId(), chatRoomId);
            chatMessageDto.setRoomId(chatRoomId);
        }

        try {
            User senderUser = userService.getUserById(chatMessageDto.getSenderId());
            String senderNickname;

            if (senderUser != null) {
                senderNickname = senderUser.getNickname();
                chatMessageDto.setSenderName(senderNickname);
            } else {
                senderNickname = "알 수 없는 사용자";
                chatMessageDto.setSenderName(senderNickname);
                log.error("오류: 메시지를 보낸 사용자 ID {}를 찾을 수 없습니다. 메시지 저장 및 브로드캐스트를 건너뜜.", chatMessageDto.getSenderId());
                return;
            }

            String dbMessageType;
            if (chatMessageDto.getMessageType() == ChatMessageDTO.MessageType.TALK ||
                    chatMessageDto.getMessageType() == ChatMessageDTO.MessageType.PAYMENT_REQUEST) {
                dbMessageType = "TEXT";
            } else if (chatMessageDto.getMessageType() == ChatMessageDTO.MessageType.JOIN ||
                    chatMessageDto.getMessageType() == ChatMessageDTO.MessageType.LEAVE) {
                dbMessageType = "SYSTEM";
            } else if (chatMessageDto.getMessageType() != null) {
                dbMessageType = chatMessageDto.getMessageType().name();
            } else {
                dbMessageType = "TEXT";
            }

            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    chatMessageDto.getImgUrl(),
                    dbMessageType
            );
            log.info("메시지 DB 저장 성공 (ChatService 호출) - RoomID: {}", chatMessageDto.getRoomId());

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생: {}", e.getMessage(), e);
            return;
        }

        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto);
        log.info("메시지 [{}]를 [{}] 경로로 브로드캐스트 완료. 발신자: {}, RoomID: {}",
                chatMessageDto.getMessage(), destination, chatMessageDto.getSenderName(), chatRoomId);
    }

    /**
     * 클라이언트가 채팅방에 입장했을 때 처리하는 메서드.
     * (이전 ChatController의 addUser 로직을 그대로 가져옴)
     *
     * @param chatMessageDto 입장 메시지 (ChatMessageDTO)
     * @param chatRoomId 입장한 채팅방 ID
     */
    @MessageMapping("/pub/chat.addUser/{chatRoomId}") // ⭐ ChatController와 동일한 경로를 사용.
    // 이제 ChatController에 이 매핑이 없으므로 충돌 없음.
    public void addUser(@Payload ChatMessageDTO chatMessageDto,
                        @DestinationVariable Long chatRoomId) {

        log.info("사용자 입장 요청 수신 - 룸ID(URL): {}, DTO RoomID: {}, 발신자 ID: {}",
                chatRoomId, chatMessageDto.getRoomId(), chatMessageDto.getSenderId());

        if (chatMessageDto.getRoomId() == null || !chatMessageDto.getRoomId().equals(chatRoomId)) {
            log.warn("경고: DTO의 chatRoomId({})와 URL의 chatRoomId({})가 일치하지 않습니다. URL의 ID를 사용합니다.",
                    chatMessageDto.getRoomId(), chatRoomId);
            chatMessageDto.setRoomId(chatRoomId);
        }

        User senderUser = userService.getUserById(chatMessageDto.getSenderId());
        String senderNickname;

        if (senderUser != null) {
            senderNickname = senderUser.getNickname();
        } else {
            senderNickname = "알 수 없는 사용자";
            log.error("오류: 입장 메시지를 보낸 사용자 ID {}를 찾을 수 없습니다.", chatMessageDto.getSenderId());
        }
        chatMessageDto.setSenderName(senderNickname);

        String joinMessage = chatMessageDto.getMessage();
        if (joinMessage == null || joinMessage.trim().isEmpty()) {
            joinMessage = senderNickname + "님이 입장하셨습니다.";
        } else {
            joinMessage = senderNickname + joinMessage;
        }
        chatMessageDto.setMessage(joinMessage);

        chatMessageDto.setMessageType(ChatMessageDTO.MessageType.JOIN); // 이전에 SYSTEM으로 저장했지만 JOIN으로 설정
        chatMessageDto.setSendTime(LocalDateTime.now());

        try {
            chatService.saveChatMessage(
                    chatMessageDto.getRoomId(),
                    chatMessageDto.getSenderId(),
                    chatMessageDto.getMessage(),
                    "",
                    chatMessageDto.getMessageType().name()
            );
            log.info("입장 메시지 DB 저장 성공 - RoomID: {}, Sender: {}", chatRoomId, senderNickname);
        } catch (Exception e) {
            log.error("입장 메시지 DB 저장 중 오류 발생: {}", e.getMessage(), e);
        }

        String destination = "/sub/chat/room/" + chatRoomId;
        messagingTemplate.convertAndSend(destination, chatMessageDto);
        log.info("사용자 입장 메시지 브로드캐스트 완료 - User: {}, Room: {}", senderNickname, chatRoomId);
    }
}